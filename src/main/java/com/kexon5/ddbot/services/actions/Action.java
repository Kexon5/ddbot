package com.kexon5.ddbot.services.actions;

import com.kexon5.ddbot.services.Buttonable;
import com.kexon5.ddbot.services.messages.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

public interface Action extends Buttonable {
    Message handleRequest(Update update);

}
