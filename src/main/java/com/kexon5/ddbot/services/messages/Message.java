package com.kexon5.ddbot.services.messages;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.io.Serializable;
import java.util.List;

public record Message(List<BotApiMethod<? extends Serializable>> msgList, boolean isLastMsg) {

    public Message(List<BotApiMethod<? extends Serializable>> msgList) {
        this(msgList, false);
    }

    public Message(BotApiMethod<? extends Serializable> msg) {
        this(List.of(msg), false);
    }
}
