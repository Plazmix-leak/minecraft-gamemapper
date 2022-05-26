package org.stonlexx.minecraft.gamemapper.log;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import net.minecrell.terminalconsole.SimpleTerminalConsole;
import org.stonlexx.minecraft.gamemapper.MinecraftGameMapper;
import org.stonlexx.minecraft.gamemapper.command.CommandManager;
import org.stonlexx.minecraft.gamemapper.command.CommandSender;

@Log4j2
@RequiredArgsConstructor
public final class LogTerminal
        extends SimpleTerminalConsole {

    @Override
    protected boolean isRunning() {
        return MinecraftGameMapper.INSTANCE.isRunning();
    }

    @Override
    protected void runCommand(String command) {

        if (!CommandManager.INSTANCE.dispatchCommand(CommandSender.INSTANCE, command)) {
            log.info(":: That command is`nt exists! Type \"/help\" for help.");
        }
    }

    @Override
    protected void shutdown() {
        MinecraftGameMapper.INSTANCE.onShutdown();
    }

}
