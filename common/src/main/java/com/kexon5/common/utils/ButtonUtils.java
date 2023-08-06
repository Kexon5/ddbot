package com.kexon5.common.utils;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.Collection;
import java.util.List;

public class ButtonUtils {

    public static <T> ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder getReplyKeyboardMarkupBuilder(Collection<T> buttons) {
        return ReplyKeyboardMarkup.builder()
                .keyboard(buttons.stream().map(T::toString)
                                          .map(KeyboardButton::new)
                                          .map(b -> new KeyboardRow(List.of(b)))
                                          .toList())
                .resizeKeyboard(true)
                .oneTimeKeyboard(true);
    }

    public static InlineKeyboardMarkup getMenu(Collection<InlineKeyboardButton> buttons) {
        return InlineKeyboardMarkup.builder()
                                   .keyboard(buttons.stream()
                                                    .map(List::of)
                                                    .toList())
                                   .build();
    }

    public static InlineKeyboardButton getButton(String callbackData, String text) {
        return InlineKeyboardButton.builder()
                                   .text(text)
                                   .callbackData(callbackData)
                                   .build();
    }
}
