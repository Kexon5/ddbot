package com.kexon5.ddbot.util;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public class Keyboards {
    private final static InlineKeyboardButton ONE_MIN_BUTTON = InlineKeyboardButton.builder().text("1 мин")
            .callbackData("1min").build();
    private final static InlineKeyboardButton THIRTY_SEC_BUTTON = InlineKeyboardButton.builder().text("30 сек")
            .callbackData("30sec").build();
    private final static InlineKeyboardButton LIST_OF_TIMERS_BUTTON = InlineKeyboardButton.builder()
            .text("Список активных таймеров").callbackData("reminders").build();
    private final static InlineKeyboardButton MAIN_BUTTON = InlineKeyboardButton.builder().text("Основное меню")
            .callbackData("mainMenu").build();
    private final static InlineKeyboardButton STOP_TIMER_BUTTON = InlineKeyboardButton.builder().text("Остановить таймер")
            .callbackData("stopTimer").build();
    public final static InlineKeyboardMarkup MAIN_MENU = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(ONE_MIN_BUTTON, THIRTY_SEC_BUTTON)).keyboardRow(List.of(LIST_OF_TIMERS_BUTTON)).build();
    public final static InlineKeyboardMarkup SUCCESS_TIMER_MENU = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(MAIN_BUTTON, LIST_OF_TIMERS_BUTTON)).build();
    public final static InlineKeyboardMarkup LIST_TIMERS_MENU = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(STOP_TIMER_BUTTON)).keyboardRow(List.of(MAIN_BUTTON)).build();
    public final static InlineKeyboardMarkup TO_MAIN_MENU = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(MAIN_BUTTON)).build();
    public final static InlineKeyboardMarkup TIMER_WAS_DELETED = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(MAIN_BUTTON)).keyboardRow(List.of(LIST_OF_TIMERS_BUTTON)).build();
}
