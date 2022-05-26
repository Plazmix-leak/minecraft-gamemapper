package org.stonlexx.minecraft.gamemapper.scheduler;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.stonlexx.minecraft.gamemapper.MinecraftGameMapper;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
public abstract class SchedulerTask implements Runnable {

    @Getter
    private final String identifier;

    public SchedulerTask() {
        this(RandomStringUtils.randomAlphanumeric(32));
    }


    /**
     * Отмена и закрытие потока
     */
    public void cancel() {
        MinecraftGameMapper.INSTANCE.getSchedulerManager().cancelScheduler(identifier);
    }

    /**
     * Запустить асинхронный поток
     */
    public void runAsync() {
        MinecraftGameMapper.INSTANCE.getSchedulerManager().runAsync(this);
    }

    /**
     * Запустить поток через определенное
     * количество времени
     *
     * @param delay - время
     * @param timeUnit - единица времени
     */
    public void runLater(long delay, TimeUnit timeUnit) {
        MinecraftGameMapper.INSTANCE.getSchedulerManager().runLater(identifier, this, delay, timeUnit);
    }

    /**
     * Запустить цикличный поток через
     * определенное количество времени
     *
     * @param delay - время
     * @param period - период цикличного воспроизведения
     * @param timeUnit - единица времени
     */
    public void runTimer(long delay, long period, TimeUnit timeUnit) {
        MinecraftGameMapper.INSTANCE.getSchedulerManager().runTimer(identifier, this, delay, period, timeUnit);
    }

}
