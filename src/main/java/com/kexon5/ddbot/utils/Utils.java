package com.kexon5.ddbot.utils;

import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static InlineKeyboardMarkup getMenu(List<InlineKeyboardButton> buttons) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        buttons.forEach(button -> keyboard.add(List.of(button)));

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public static Long getIdFromMessage(Update update) {
        return update.getMessage().getFrom().getId();
    }

    public static Long getIdFromCallback(Update update) {
        return update.getCallbackQuery().getFrom().getId();
    }

    public static Long getId(Update update) {
        return (update.hasMessage()) ? getIdFromMessage(update) : getIdFromCallback(update);
    }

}
