package org.stonlexx.minecraft.gamemapper.command;

import lombok.Getter;
import lombok.NonNull;

@Getter
public abstract class MinecraftCommand {

    private final String commandName;
    private final String[] commandAliases;

    public MinecraftCommand(@NonNull String commandName, @NonNull String... commandAliases) {
        this.commandName = commandName;
        this.commandAliases = commandAliases;
    }

    /**
     * Действия выполнения команды от имени любого
     * ее возможного отправителя
     *
     * @param commandSender - отправитель команды
     * @param commandArgs   - аргументы команды
     */
    public abstract void onExecute(@NonNull CommandSender commandSender, @NonNull String[] commandArgs);

}
