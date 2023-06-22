package com.kexon5.ddbot.statemachine;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class MessageHolder {

    private final BotApiMethod<? extends Serializable> msg;
    private Integer msgId;

    public void setMsgId(Integer msgId) {
        this.msgId = msgId;
    }
}
