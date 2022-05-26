package org.stonlexx.minecraft.gamemapper.command.type;

import lombok.NonNull;
import org.stonlexx.minecraft.gamemapper.MinecraftGameMapper;
import org.stonlexx.minecraft.gamemapper.command.CommandSender;
import org.stonlexx.minecraft.gamemapper.command.MinecraftCommand;

public final class CommandShutdown extends MinecraftCommand {

    public CommandShutdown() {
        super("shutdown", "end", "stop");
    }

    @Override
    public void onExecute(@NonNull CommandSender commandSender, @NonNull String[] args) {
        MinecraftGameMapper.INSTANCE.onShutdown();
    }

}
