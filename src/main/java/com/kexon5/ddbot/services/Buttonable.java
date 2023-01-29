package com.kexon5.ddbot.services;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

public interface Buttonable {
    String getButtonText();

    InlineKeyboardButton getButton();
}
