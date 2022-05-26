package org.stonlexx.minecraft.gamemapper.objects;

import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.stonlexx.minecraft.gamemapper.MinecraftGameMapper;
import org.stonlexx.minecraft.gamemapper.starter.ServerStarterService;
import org.stonlexx.minecraft.gamemapper.utility.ChatColor;
import org.stonlexx.minecraft.gamemapper.utility.Directories;

import java.io.File;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Log4j2
@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TemplateServer {

    @SuppressWarnings("all")
    @SneakyThrows
    public static TemplateServer create(@NonNull Path directory) {
        String name = directory.toFile().getName();
        File propertiesFile = directory.resolve("settings.properties").toFile();

        if (!Files.exists(propertiesFile.toPath())) {
            Files.copy(MinecraftGameMapper.class.getClassLoader().getResourceAsStream("default_settings.properties"), propertiesFile.toPath());
        }

        TemplateServer templateServer = new TemplateServer(name, directory, propertiesFile.toPath(), new Properties());
        templateServer.reloadProperties();

        return templateServer;
    }


    @NonNull String name;

    @NonNull Path directory;
    @NonNull Path propertiesFile;

    @NonNull Properties properties;

    @NonNull Map<String, ServerStarterService> activeServersMap = new HashMap<>();
    @NonNull Map<String, ServerStarterService> runningServersMap = new HashMap<>();

    @NonNull ExecutorService executor = Executors.newCachedThreadPool();


    @SneakyThrows
    public void reloadProperties() {

        try (FileReader fileReader = new FileReader(propertiesFile.toFile())) {
            properties.load(fileReader);
        }
    }


    private BukkitServerParser getServerParser() {
        ServerStarterService serverStarterService = activeServersMap.get(name);

        if (serverStarterService == null || serverStarterService.getAddress() == null) {
            return null;
        }

        BukkitServerParser parser = new BukkitServerParser(serverStarterService.getAddress().getHostString(), serverStarterService.getAddress().getPort());
        parser.connectInit();

        return parser;
    }

    public int getOnline(@NonNull String name) {
        BukkitServerParser parser = getServerParser();

        if (parser == null) {
            return 0;
        }

        return parser.getMaxOnline();
    }

    public int getMaxOnline(@NonNull String name) {
        BukkitServerParser parser = getServerParser();

        if (parser == null) {
            return 0;
        }

        return parser.getMaxOnline();
    }

    public int getTotalOnline() {
        return getActiveServers()
                .stream()
                .mapToInt(this::getOnline)
                .sum();
    }

    public int getTotalMaxOnline() {
        return getActiveServers()
                .stream()
                .mapToInt(this::getMaxOnline)
                .sum();
    }


    public int getStartPort() {
        return Integer.parseInt(properties.getProperty("start.port", "10000"));
    }

    public String getMemory() {
        return properties.getProperty("total.memory", "512M");
    }


    public Set<String> getActiveServers() {
        return activeServersMap.keySet();
    }

    public boolean isRunning(@NonNull String name) {
        return runningServersMap.containsKey(name.toLowerCase()) || activeServersMap.containsKey(name);
    }

    public boolean isActive(@NonNull String name) {
        return activeServersMap.containsKey(name);
    }

    public void onActivated(@NonNull String name, @NonNull ServerStarterService starterService) {
        runningServersMap.remove(name.toLowerCase());
        activeServersMap.put(name, starterService);
    }

    public void onShutdown(@NonNull String name) {
        Directories.clearDirectory(MinecraftGameMapper.INSTANCE.getRunningPath().resolve(name).toFile(), true);

        runningServersMap.remove(name.toLowerCase()); //  на всякий случай
        activeServersMap.remove(name);

        // Из-за получения некоторых данных этот алгоритм может
        // быть не самым быстрым, поэтому выполним его асинхронно.
        executor.submit(() -> {

            // Variables for new servers count for start new servers.
            int defaultServersCount = Integer.parseInt(getProperties().getProperty("default.servers.count", "3"));

            // Variables for total servers online percent for start new servers.
            double maxPlayersPercent = 95.8D;
            int currentOnlinePercent = (getTotalOnline() * 100) / getTotalMaxOnline();

            // Checking variables data.
            if (activeServersMap.size() < defaultServersCount || currentOnlinePercent < maxPlayersPercent) {

                // Check new CPU percent value for start new servers.
                double maxCpuValue = 90.0D;
                double currentCpuValue = (MinecraftGameMapper.INSTANCE.getSystemMxBean().getSystemCpuLoad() * 100);

                if (currentCpuValue < maxCpuValue) {

                    for (int i = 0; i < defaultServersCount; i++) {
                        ServerStarterService server = startNewServer();

                        log.warn(ChatColor.YELLOW + "[Servers] :: Server " + server.getServerName() + " was running (CPU:" + Math.round(currentCpuValue) + "%, Online:" + Math.round(currentOnlinePercent) + "%)");
                    }

                } else {

                    log.warn(ChatColor.RED + "[Servers] :: Low percent CPU value: " + Math.round(currentCpuValue) + "%, can`t be create a new server!");
                }
            }
        });
    }

    public ServerStarterService startNewServer() {
        ServerStarterService starterService = new ServerStarterService(this);
        starterService.runServer();

        runningServersMap.put(starterService.getServerName().toLowerCase(), starterService);

        return starterService;
    }

}
