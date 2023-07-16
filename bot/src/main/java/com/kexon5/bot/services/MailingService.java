package com.kexon5.bot.services;

import com.kexon5.bot.bot.DDBot;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalUnit;

@RequiredArgsConstructor
public class MailingService {

    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;
    private final DDBot bot;

    public void addBeforeDateMsg(String msg,
                                 long userId,
                                 LocalDateTime timePoint,
                                 long timeShift,
                                 TemporalUnit temporalUnit) {
        threadPoolTaskScheduler.schedule(
                () -> bot.silent().send(msg, userId),
                timePoint.minus(timeShift, temporalUnit).toInstant(ZoneOffset.ofHours(3))
        );
    }

    public void addAfterDateMsg(String msg,
                                long userId,
                                LocalDateTime timePoint,
                                long timeShift,
                                TemporalUnit temporalUnit) {
        threadPoolTaskScheduler.schedule(
                () -> bot.silent().send(msg, userId),
                timePoint.plus(timeShift, temporalUnit).toInstant(ZoneOffset.ofHours(3))
        );
    }

    public void addBeforeDateMsg(BotApiMethod<? extends Serializable> msg,
                                 LocalDateTime timePoint,
                                 long timeShift,
                                 TemporalUnit temporalUnit) {
        threadPoolTaskScheduler.schedule(
                () -> bot.silent().execute(msg),
                timePoint.minus(timeShift, temporalUnit).toInstant(ZoneOffset.ofHours(3))
        );
    }

    public void addAfterDateMsg(BotApiMethod<? extends Serializable> msg,
                                LocalDateTime timePoint,
                                long timeShift,
                                TemporalUnit temporalUnit) {
        threadPoolTaskScheduler.schedule(
                () -> bot.silent().execute(msg),
                timePoint.plus(timeShift, temporalUnit).toInstant(ZoneOffset.ofHours(3))
        );
    }

    public void addRepeatableMsg(BotApiMethod<? extends Serializable> msg,
                                 long timeShift,
                                 TemporalUnit temporalUnit) {
        threadPoolTaskScheduler.scheduleAtFixedRate(
                () -> bot.silent().execute(msg),
                Duration.of(timeShift, temporalUnit)
        );
    }

}
