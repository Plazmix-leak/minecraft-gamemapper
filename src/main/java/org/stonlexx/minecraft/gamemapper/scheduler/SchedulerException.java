package org.stonlexx.minecraft.gamemapper.scheduler;

public class SchedulerException extends Exception {

    /**
     * Вызов exception
     *
     * @param errorMessage - сообщение ошибки
     * @param elements - элементы, которые нужно заменить в строке
     */
    public SchedulerException(String errorMessage, Object... elements) {
        super( String.format(errorMessage, elements) );
    }

}
