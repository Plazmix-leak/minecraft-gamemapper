package org.stonlexx.minecraft.gamemapper.command;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;

import java.util.*;

@Log4j2
public final class CommandManager {

    public static final CommandManager INSTANCE = new CommandManager();


    private final Map<String, MinecraftCommand> baseCommands = new HashMap<>();

    public void registerCommand(@NonNull MinecraftCommand baseCommand) {
        baseCommands.put(baseCommand.getCommandName().toLowerCase(), baseCommand);

        for (String commandAlias : baseCommand.getCommandAliases()) {
            baseCommands.put(commandAlias.toLowerCase(), baseCommand);
        }
    }

    public MinecraftCommand getCommand(@NonNull String commandLine) {
        return baseCommands.get(commandLine.toLowerCase());
    }

    public boolean dispatchCommand(@NonNull CommandSender commandSender, @NonNull String commandLine) {
        if (!commandLine.startsWith("/")) {
            commandLine = ("/" + commandLine);
        }

        String[] commandLineSplit = commandLine.substring(1).split("\\s+");

        String commandLabel = commandLineSplit[0].toLowerCase();
        String[] commandArgs = Arrays.copyOfRange(commandLineSplit, 1, commandLineSplit.length);

        MinecraftCommand baseCommand = getCommand(commandLabel);

        if (baseCommand == null) {
            return false;
        }

        log.info("[Command] Dispatched command - " + commandLine);
        baseCommand.onExecute(commandSender, commandArgs);

        return true;
    }

    public Collection<MinecraftCommand> getRegisteredCommands() {
        return new HashSet<>(baseCommands.values());
    }

}
