package com.kexon5.ddbot.services;

import com.kexon5.ddbot.services.messages.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public interface Service extends Buttonable {
    Message getMenu(Update update);

    String getMainMessage(Update update);

    List<InlineKeyboardButton> getInternalServices();

    List<InlineKeyboardButton> getActions();

    InlineKeyboardButton getMainMenuButton();
}
