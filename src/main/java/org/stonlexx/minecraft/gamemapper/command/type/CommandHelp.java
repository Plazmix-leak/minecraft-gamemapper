package org.stonlexx.minecraft.gamemapper.command.type;

import lombok.NonNull;
import org.stonlexx.minecraft.gamemapper.command.CommandSender;
import org.stonlexx.minecraft.gamemapper.command.MinecraftCommand;

public final class CommandHelp extends MinecraftCommand {

    public CommandHelp() {
        super("help", "/help", "путинпомоги");
    }

    @Override
    public void onExecute(@NonNull CommandSender commandSender, @NonNull String[] args) {
        commandSender.sendMessage("§b§l[GameMapper] :: §fCommands help list:");

        commandSender.sendMessage(" §f| §7Reload the system - §c/reload <(true/false): Allow to clear and stop all active servers>");

        commandSender.sendMessage(" §f| §7Create a new template - §c/template <template name>");
        commandSender.sendMessage(" §f| §7Start the template servers - §c/start <template name/*>");

        commandSender.sendMessage(" §f| §7Print the system stats - §c/stats");

        commandSender.sendMessage(" §f| §7Shutdown the system - §c/stop/shutdown/end");
    }

}
