package org.stonlexx.minecraft.gamemapper.command;

import lombok.NonNull;
import lombok.extern.log4j.Log4j2;
import org.apache.logging.log4j.Level;

@Log4j2
public final class CommandSender {

    public static final CommandSender INSTANCE = new CommandSender();

    /**
     * Отправить сообщение
     *
     * @param message - текст сообщения
     */
    public void sendMessage(@NonNull String message) {
        log.info(message);
    }

    /**
     * Отправить сообщение
     *
     * @param level - уровень приоритета сообщения
     * @param message - текст сообщения
     */
    public void sendMessage(@NonNull Level level, @NonNull String message) {
        log.log(level, message);
    }
}
