package org.stonlexx.minecraft.gamemapper.command.type;

import lombok.NonNull;
import org.stonlexx.minecraft.gamemapper.MinecraftGameMapper;
import org.stonlexx.minecraft.gamemapper.command.CommandSender;
import org.stonlexx.minecraft.gamemapper.command.MinecraftCommand;
import org.stonlexx.minecraft.gamemapper.utility.ChatColor;

public final class CommandReload extends MinecraftCommand {

    public static final String USAGE_FORMAT = (ChatColor.RED + "[Commands] :: Use - /reload <(true/false): Allow to clear and stop all active servers>");

    public CommandReload() {
        super("reload");
    }

    @Override
    public void onExecute(@NonNull CommandSender commandSender, @NonNull String[] args) {
        if (args.length == 0) {
            commandSender.sendMessage(USAGE_FORMAT);
            return;
        }

        boolean canClearAndStop = Boolean.parseBoolean(args[0]);

        if (!canClearAndStop && !args[0].equalsIgnoreCase("false")) {
            commandSender.sendMessage(USAGE_FORMAT);
            return;
        }

        MinecraftGameMapper.INSTANCE.loadTemplateServers(canClearAndStop);
    }

}
