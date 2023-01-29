package com.kexon5.ddbot.services.messages;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;
import java.util.List;

public interface MessageCreator {

    List<BotApiMethod<? extends Serializable>> getMessage(Update update);

}
