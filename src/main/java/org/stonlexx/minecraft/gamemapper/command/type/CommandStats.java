package org.stonlexx.minecraft.gamemapper.command.type;

import lombok.NonNull;
import org.stonlexx.minecraft.gamemapper.MinecraftGameMapper;
import org.stonlexx.minecraft.gamemapper.command.CommandSender;
import org.stonlexx.minecraft.gamemapper.command.MinecraftCommand;
import org.stonlexx.minecraft.gamemapper.utility.DateUtil;
import org.stonlexx.minecraft.gamemapper.utility.NumberUtil;

import java.util.Objects;

public final class CommandStats extends MinecraftCommand {

    public CommandStats() {
        super("stats");
    }

    @Override
    public void onExecute(@NonNull CommandSender commandSender, @NonNull String[] commandArgs) {
        MinecraftGameMapper systemInstance = MinecraftGameMapper.INSTANCE;
        commandSender.sendMessage("§b§l[GameMapper] :: §fSystem statistics:");

        // Memory statistics.
        Runtime runtime = Runtime.getRuntime();

        commandSender.sendMessage("");
        commandSender.sendMessage("  §c§l:: §nMEMORY:");
        commandSender.sendMessage("   §fMax:       §e" + (runtime.maxMemory() / 1024L / 1024L) + " MB");
        commandSender.sendMessage("   §fTotal:     §e" + (runtime.totalMemory() / 1024L / 1024L) + " MB");
        commandSender.sendMessage("   §fFree:      §e" + (runtime.freeMemory() / 1024L / 1024L) + " MB");
        commandSender.sendMessage("   §fUsed:      §e" + ((runtime.totalMemory() - runtime.freeMemory()) / 1024L / 1024L) + " MB");

        // Operation System statistics.
        commandSender.sendMessage("");
        commandSender.sendMessage("  §c§l:: §nOPERATION SYSTEM:");
        commandSender.sendMessage("   §fName:      §b" + systemInstance.getSystemMxBean().getName());
        commandSender.sendMessage("   §fVersion:   §b" + systemInstance.getSystemMxBean().getVersion());
        commandSender.sendMessage("   §fCores:     §b" + runtime.availableProcessors() / 2);
        commandSender.sendMessage("   §fThreads:   §b" + runtime.availableProcessors());

        // CPU statistics.
        commandSender.sendMessage("");
        commandSender.sendMessage("  §c§l:: §nCPU:");
        commandSender.sendMessage("   §fSystem:    §7" + Math.ceil(systemInstance.getSystemMxBean().getSystemCpuLoad() * 100) + "%");
        commandSender.sendMessage("   §fProcess:   §7" + Math.ceil(systemInstance.getSystemMxBean().getProcessCpuLoad() * 100) + "%");

        // Servers statistics.
        commandSender.sendMessage("");
        commandSender.sendMessage("  §c§l:: §nSERVERS:");
        commandSender.sendMessage("   §fTemplates: §a" + systemInstance.getTemplateServersMap().size());
        commandSender.sendMessage("   §fActives:   §a" + systemInstance.getTemplateServersMap().values().stream().mapToInt(server -> server.getActiveServersMap().size()).sum());
        commandSender.sendMessage("   §fRunning:   §a" + systemInstance.getTemplateServersMap().values().stream().mapToInt(server -> server.getRunningServersMap().size()).sum());

        // Uptime statistics.
        commandSender.sendMessage("");
        commandSender.sendMessage(" §f:: System Uptime: §7" + NumberUtil.parseClockTime(systemInstance.getUptimeMillis())
                + " §b(Running on " + DateUtil.formatTime(systemInstance.getStartTimeMillis(), "dd MMM yyyy HH:mm:ss") + ")");
    }

}
