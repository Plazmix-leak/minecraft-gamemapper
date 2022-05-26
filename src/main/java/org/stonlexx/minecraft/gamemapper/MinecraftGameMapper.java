package org.stonlexx.minecraft.gamemapper;

import com.sun.management.OperatingSystemMXBean;
import lombok.*;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.stonlexx.minecraft.gamemapper.command.CommandManager;
import org.stonlexx.minecraft.gamemapper.command.type.*;
import org.stonlexx.minecraft.gamemapper.log.LogTerminal;
import org.stonlexx.minecraft.gamemapper.objects.TemplateServer;
import org.stonlexx.minecraft.gamemapper.scheduler.SchedulerManager;
import org.stonlexx.minecraft.gamemapper.starter.ServerStarterService;
import org.stonlexx.minecraft.gamemapper.utility.ChatColor;
import org.stonlexx.minecraft.gamemapper.utility.Directories;
import org.stonlexx.minecraft.gamemapper.utility.ProcessExecutionUtil;

import java.io.File;
import java.lang.management.ManagementFactory;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Setter
@Getter
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Log4j2
public final class MinecraftGameMapper {

    public static final MinecraftGameMapper INSTANCE = new MinecraftGameMapper();


    Logger logger = log;

    Path globalTemplatePath = Paths.get("global-template");
    Path templatesPath      = Paths.get("template");
    Path runningPath        = Paths.get("run");

    Map<String, TemplateServer> templateServersMap  = new HashMap<>();
    SchedulerManager schedulerManager               = new SchedulerManager();

    OperatingSystemMXBean systemMxBean              = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);

    @NonFinal
    long startTimeMillis;

    @NonFinal
    boolean running;


    private void registerCommands() {
        CommandManager.INSTANCE.registerCommand(new CommandReload());
        CommandManager.INSTANCE.registerCommand(new CommandHelp());
        CommandManager.INSTANCE.registerCommand(new CommandCreateTemplate());
        CommandManager.INSTANCE.registerCommand(new CommandServersStart());
        CommandManager.INSTANCE.registerCommand(new CommandShutdown());
        CommandManager.INSTANCE.registerCommand(new CommandStats());
    }

    @SneakyThrows
    void onStart() {
        setRunning(true);

        // If started that on the windows?
        if (System.getProperty("os.name").toLowerCase().startsWith("windows")) {

            log.warn(ChatColor.DARK_RED + "[GameMapper] :: Windows OS is`nt supported!");
            log.warn(ChatColor.DARK_RED + "[GameMapper] :: System exit for 3 seconds...");

            log.log(Level.OFF, "   ");

            Thread.sleep(TimeUnit.SECONDS.toMillis(3));
            onShutdown();
            return;
        }

        // Create needed directories (if not exists)
        Directories.createIfNotExists(globalTemplatePath.toFile());
        Directories.createIfNotExists(templatesPath.toFile());
        Directories.createIfNotExists(runningPath.toFile());

        // Register system services.
        reloadTemplates();
        registerCommands();

        // Start the logger task.
        this.startTimeMillis = System.currentTimeMillis();

        new LogTerminal().start();
    }

    public void onShutdown() {
        setRunning(false);

        log.info(ChatColor.RED + "[GameMapper] :: Thanks for using Minecraft-GameMapper system!");
        log.info(ChatColor.RED + "[GameMapper] :: Author: [ItzStonlex] (Discord: https://discord.gg/j4eD7FuwBp)");

        for (TemplateServer templateServer : templateServersMap.values()) {
            for (ServerStarterService activeServer : templateServer.getActiveServersMap().values()) {
                activeServer.shutdownServer();
            }

            for (ServerStarterService runningServer : templateServer.getRunningServersMap().values()) {
                runningServer.shutdownServer();
            }
        }

        ProcessExecutionUtil.destroyAll();

        Directories.clearDirectory(runningPath.toFile(), false);
        System.exit(0);
    }

    public long getUptimeMillis() {
        return System.currentTimeMillis() - startTimeMillis;
    }


    public TemplateServer getTemplateServer(@NonNull String server) {
        return templateServersMap.get(server.toLowerCase());
    }

    public TemplateServer createTemplate(@NonNull String server) {
        TemplateServer templateServer = TemplateServer.create(getTemplatesPath().resolve(server));
        templateServersMap.put(server.toLowerCase(), templateServer);

        return templateServer;
    }

    @SneakyThrows
    public Collection<TemplateServer> reloadTemplates() {
        Collection<TemplateServer> newTemplatesList = new ArrayList<>();
        long currentTimeMillis = System.currentTimeMillis();

        if (!Files.exists(getTemplatesPath())) {
            Files.createDirectories(getTemplatesPath());
        }

        for (File templateDirectory : Objects.requireNonNull(getTemplatesPath().toFile().listFiles())) {
            String templateName = templateDirectory.getName();

            if (!templateServersMap.containsKey(templateName.toLowerCase())) {
                TemplateServer templateServer = TemplateServer.create(templateDirectory.toPath());

                log.info(ChatColor.GREEN + "[Templates] :: " + templateServer.getName() + " was success loaded");

                templateServersMap.put(templateName.toLowerCase(), templateServer);
                newTemplatesList.add(templateServer);

            } else {

                TemplateServer templateServer = getTemplateServer(templateName);
                templateServer.reloadProperties();

                log.info(ChatColor.GREEN + "[Templates] :: Property " + templateServer.getName() + " was success reloaded");
            }
        }

        log.info(ChatColor.GREEN + "[Templates] :: Success loaded " + templateServersMap.size() + " templates for "
                + (System.currentTimeMillis() - currentTimeMillis) + "ms");

        return newTemplatesList;
    }

    @SneakyThrows
    public void loadTemplateServers(boolean clearAndStop) {
        reloadTemplates();
        startCachedTemplates0(clearAndStop);
    }

    private void startTemplate0(boolean clearAndStop, @NonNull TemplateServer templateServer) {
        int defaultServersCount = Integer.parseInt(templateServer.getProperties().getProperty("default.servers.count", "3"));

        if (templateServer.getActiveServers().size() >= defaultServersCount && !clearAndStop) {
            return;
        }

        for (int i = 0; i < defaultServersCount; i++) {
            templateServer.startNewServer();
        }
    }

    public void startTemplate(@NonNull TemplateServer templateServer) {
        startTemplate0(true, templateServer);
    }

    private void startCachedTemplates0(boolean clearAndStop) {
        for (TemplateServer templateServer : templateServersMap.values()) {
            startTemplate0(clearAndStop, templateServer);
        }
    }

    public void startCachedTemplates() {
        startCachedTemplates0(false);
    }

}
