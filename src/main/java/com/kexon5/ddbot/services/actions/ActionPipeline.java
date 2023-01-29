package com.kexon5.ddbot.services.actions;

import com.kexon5.ddbot.services.messages.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface ActionPipeline {
    Message test(int state, Update update);
    int size();
    Message getMessage(int state, Update update);
    Message getFinalMessage(Update update);
}
