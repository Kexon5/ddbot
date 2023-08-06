package com.kexon5.common.services;

import com.kexon5.common.repositories.MailingGroupRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.io.Serializable;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalUnit;
import java.util.Set;

@RequiredArgsConstructor
public class MailingService {

    public static final String NEW_TABLES = "NEW_TABLES";


    private final MailingGroupRepository mailingGroupRepository;

    private final ThreadPoolTaskScheduler threadPoolTaskScheduler;
    @Setter
    private SilentSender sender;

    public void addBeforeDateMsg(String msg,
                                 long userId,
                                 LocalDateTime timePoint,
                                 long timeShift,
                                 TemporalUnit temporalUnit) {
        threadPoolTaskScheduler.schedule(
                () -> sender.send(msg, userId),
                timePoint.minus(timeShift, temporalUnit).toInstant(ZoneOffset.ofHours(3))
        );
    }

    public void addAfterDateMsg(String msg,
                                long userId,
                                LocalDateTime timePoint,
                                long timeShift,
                                TemporalUnit temporalUnit) {
        SendMessage sendMessage = SendMessage.builder()
                                             .text(msg)
                                             .chatId(userId)
                                             .parseMode("Markdown")
                                             .build();
        threadPoolTaskScheduler.schedule(
                () -> sender.executeAsync(sendMessage, c -> {}),
                timePoint.plus(timeShift, temporalUnit).toInstant(ZoneOffset.ofHours(3))
        );
    }

    public void addBeforeDateMsg(BotApiMethod<? extends Serializable> msg,
                                 LocalDateTime timePoint,
                                 long timeShift,
                                 TemporalUnit temporalUnit) {
        threadPoolTaskScheduler.schedule(
                () -> sender.executeAsync(msg),
                timePoint.minus(timeShift, temporalUnit).toInstant(ZoneOffset.ofHours(3))
        );
    }

    public void addAfterDateMsg(BotApiMethod<? extends Serializable> msg,
                                LocalDateTime timePoint,
                                long timeShift,
                                TemporalUnit temporalUnit) {
        threadPoolTaskScheduler.schedule(
                () -> sender.executeAsync(msg),
                timePoint.plus(timeShift, temporalUnit).toInstant(ZoneOffset.ofHours(3))
        );
    }

    public void addRepeatableMsg(BotApiMethod<? extends Serializable> msg,
                                 long timeShift,
                                 TemporalUnit temporalUnit) {
        threadPoolTaskScheduler.scheduleAtFixedRate(
                () -> sender.executeAsync(msg),
                Duration.of(timeShift, temporalUnit)
        );
    }


    public void sendMsgs(String userGroup, String msgText) {
        var userIds = mailingGroupRepository.findByGroupName(userGroup).getUsers();
        sendMsgs(userIds, msgText);
    }

    public void sendMsgs(Set<Long> userIds, String msgText) {
        var msgBuilder = SendMessage.builder()
                                    .text(msgText)
                                    .parseMode("Markdown");

        userIds.forEach(userId -> sender.executeAsync(msgBuilder.chatId(userId).build(), c -> {}));
    }

}
