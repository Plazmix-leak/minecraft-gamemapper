package org.stonlexx.minecraft.gamemapper.starter;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.log4j.Log4j2;
import org.stonlexx.minecraft.gamemapper.MinecraftGameMapper;
import org.stonlexx.minecraft.gamemapper.exception.ServerJarNotFoundException;
import org.stonlexx.minecraft.gamemapper.exception.ServerProcessException;
import org.stonlexx.minecraft.gamemapper.objects.TemplateServer;
import org.stonlexx.minecraft.gamemapper.scheduler.SchedulerTask;
import org.stonlexx.minecraft.gamemapper.utility.ChatColor;
import org.stonlexx.minecraft.gamemapper.utility.Directories;
import org.stonlexx.minecraft.gamemapper.utility.FileUtil;
import org.stonlexx.minecraft.gamemapper.utility.ProcessExecutionUtil;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Getter
@Log4j2
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ServerStarterService {

    public static final String SHELL        = "screen -h 5000 -dmS %server_name% java -server -Xmx%server_memory% -Dfile.encoding=UTF-8 -jar %jar_name%";
    public static final String SHELL_NAME   = "start.sh";

    public static final int MIN_SERVER_INDEX_RANGE = 1;
    public static final int MAX_SERVER_INDEX_RANGE = 1024;

    @NonNull
    TemplateServer templateServer;

    @NonFinal Process process;
    @NonFinal Path serverRunFolder;

    @NonFinal String serverName;
    @NonFinal InetSocketAddress address;


    @SneakyThrows
    public synchronized void runServer() {
        serverName = (templateServer.getName() + "-" + generateServerIndex());

        if (serverRunFolder == null) {
            serverRunFolder = MinecraftGameMapper.INSTANCE.getRunningPath().resolve(serverName);
        }

        // Creating the server run folder
        if (!Files.exists(serverRunFolder)) {
            Files.createDirectories(serverRunFolder);
        }

        Directories.copyDirectory(MinecraftGameMapper.INSTANCE.getGlobalTemplatePath(), serverRunFolder, templateServer.getProperties().getProperty("ignored.plugins.list", "").split(","));
        Directories.copyDirectory(templateServer.getDirectory(), serverRunFolder);

        Files.deleteIfExists(serverRunFolder.resolve("settings.properties"));

        // Start the server
        File jarFile = Arrays.stream(Objects.requireNonNull(serverRunFolder.toFile().listFiles()))
                .filter(file -> file.getName().endsWith(".jar"))
                .findFirst()
                .orElse(null);

        if (jarFile == null) {
            log.error(serverName, new ServerJarNotFoundException("file <server>.jar"));

            templateServer.getRunningServersMap().remove(serverName.toLowerCase());
            templateServer.getActiveServersMap().remove(serverName);

            Directories.clearDirectory(serverRunFolder.toFile(), true);
            return;
        }

        log.info(ChatColor.YELLOW + "[Servers] :: Handshake: Running server \"" + serverName + "\"...");
        execute(jarFile.getName());
    }

    public synchronized void shutdownServer() {
        if (process != null) {

            process.destroy();
            process = null;
        }
    }


    @NonFinal
    int lastIndex;

    private synchronized int generateServerIndex() {
        boolean canIncrement = Boolean.parseBoolean(templateServer.getProperties().getProperty("increment.server.index", "false"));

        int serverIndex = canIncrement
                ? templateServer.getActiveServers().size() + templateServer.getRunningServersMap().size() + 1
                : ThreadLocalRandom.current().nextInt(MIN_SERVER_INDEX_RANGE, MAX_SERVER_INDEX_RANGE);

        String newServerName = (templateServer.getName() + "-" + serverIndex);

        if (!canIncrement && templateServer.isRunning(newServerName)) {
            return generateServerIndex();
        }

        return lastIndex = serverIndex;
    }

    public synchronized void execute(@NonNull String jarFile) {
        Path runDirectory = MinecraftGameMapper.INSTANCE.getRunningPath().resolve(serverName);

        if (process != null && process.isAlive()) {
            return;
        }

        // create a file
        Path bashFile = serverRunFolder.resolve(SHELL_NAME);

        try {
            if (!Files.exists(bashFile)) {
                Files.createFile(bashFile);
            }
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }

        // build batch commands
        FileUtil.write(bashFile.toFile(), fileWriter -> {

            String script = SHELL
                    .replace("%jar_name%", jarFile)
                    .replace("%server_name%", serverName)
                    .replace("%server_memory%", templateServer.getMemory());

            fileWriter.write(script);
        });

        // start the process
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command("sh", "start.sh");

        processBuilder.directory(runDirectory.toFile());

        try {
            ProcessExecutionUtil.addProcessTask(process = processBuilder.start());

        } catch (IOException exception) {
            templateServer.getRunningServersMap().remove(serverName.toLowerCase());
            templateServer.getActiveServersMap().remove(serverName);

            log.error(serverName, new ServerProcessException(exception.getMessage()));

            Directories.clearDirectory(runDirectory.toFile(), true);
            return;
        }


        // create server.properties
        Properties serverProperties = new Properties();

        File serverPropertiesFile = runDirectory.resolve("server.properties").toFile();

        try {
            if (!Files.exists(serverPropertiesFile.toPath())) {
                serverPropertiesFile.createNewFile();
            }
        }
        catch (IOException exception) {
            exception.printStackTrace();
        }

        FileUtil.read(serverPropertiesFile, serverProperties::load);
        int startPort = templateServer.getStartPort();

        if (startPort > 0) {
            serverProperties.setProperty("server-port", String.valueOf(startPort + lastIndex));

        } else {

            serverProperties.setProperty("server-port", String.valueOf(ThreadLocalRandom.current().nextInt(1000, 55_555)));
        }

        serverProperties.setProperty("server-ip", "127.0.0.1");
        serverProperties.setProperty("server-name", serverName);

        FileUtil.write(serverPropertiesFile, handler -> serverProperties.store(handler, null));

        // Если сервер самостоятельно или вручную будет остановлен, то проверяем это
        checkShutdown(serverProperties);
    }


    private synchronized Bootstrap createInboundBootstrap(@NonNull Properties serverProperties) {
        return new Bootstrap()
                .remoteAddress(address = new InetSocketAddress("localhost", Integer.parseInt(serverProperties.getProperty("server-port"))))

                .channel(NioSocketChannel.class)

                .group(new NioEventLoopGroup(2))
                .handler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel channel) {
                        channel.config().setOption(ChannelOption.TCP_NODELAY, true);
                        channel.config().setAllocator(PooledByteBufAllocator.DEFAULT);

                        channel.pipeline().addLast("shutdown-handler", new ShutdownChannelHandler(serverName, serverProperties));
                    }
                });
    }

    private synchronized void checkShutdown(@NonNull Properties serverProperties) {
        Bootstrap bootstrap = createInboundBootstrap(serverProperties);

        new SchedulerTask() {

            @NonNull long timeoutCounter = 0;
            @NonNull long connectionCounter = 0;

            @Override
            public void run() {
                if (connectionCounter <= 0) {
                    timeoutCounter++;

                    if (timeoutCounter >= TimeUnit.MINUTES.toSeconds(1)) {
                        cancel();

                        templateServer.onShutdown(serverName);
                        shutdownServer();
                        return;
                    }
                }

                bootstrap.connect().addListener((ChannelFutureListener) (future) -> {
                    timeoutCounter = 0;

                    if (templateServer.isActive(serverName)) {
                        return;
                    }

                    if (future.isSuccess()) {
                        templateServer.onActivated(serverName, ServerStarterService.this);

                        log.info(ChatColor.GREEN + "[Servers] :: Server \"" + serverName + "\" was success connected!");

                        cancel();

                    } else {
                        connectionCounter++;

                        if (connectionCounter >= 30) {
                            cancel();

                            templateServer.onShutdown(serverName);
                            shutdownServer();

                            log.info(ChatColor.RED + "[Servers] :: No response server \"" + serverName + "\" connection: Timeout");
                        }
                    }
                });
            }

        }.runTimer(10, 1, TimeUnit.SECONDS);
    }

    @RequiredArgsConstructor
    private final class ShutdownChannelHandler extends ChannelInboundHandlerAdapter {

        private final String name;
        private final Properties serverProperties;

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            log.error(templateServer.getName() + " <-> " + cause.getMessage(), cause);
        }

        @Override
        public void channelInactive(ChannelHandlerContext ctx) {
            if (!templateServer.isRunning(name)) {
                return;
            }

            // Вдруг он отключился из-за какого-то таймаута или неактивности
            ServerStarterService.this.createInboundBootstrap(serverProperties).connect().addListener(future -> {

                if (!future.isSuccess()) {
                    log.info(ChatColor.RED + "[Servers] :: Handshake: Server stopped \"" + name + "\"");

                    templateServer.onShutdown(name);
                }
            });
        }
    }
}
