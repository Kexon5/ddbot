package com.kexon5.ddbot.utils;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.io.Serializable;
import java.util.List;

public record BotMessage(List<BotApiMethod<? extends Serializable>> msgList) {
    public BotMessage(BotApiMethod<? extends Serializable> msg) {
        this(List.of(msg));
    }

}
