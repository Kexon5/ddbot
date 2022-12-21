package com.kexon5.ddbot.bot;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

public class Keyboards {
    private final static InlineKeyboardButton ONE_MIN_BUTTON = InlineKeyboardButton.builder().text("1 мин")
            .callbackData(QueryCallback.ONE_MIN.getData()).build();
    private final static InlineKeyboardButton THIRTY_SEC_BUTTON = InlineKeyboardButton.builder().text("30 сек")
            .callbackData(QueryCallback.THIRTY_SEC.getData()).build();
    private final static InlineKeyboardButton LIST_OF_TIMERS_BUTTON = InlineKeyboardButton.builder()
            .text("Список активных таймеров").callbackData(QueryCallback.LIST_OF_REMINDERS.getData()).build();
    private final static InlineKeyboardButton MAIN_MENU_BUTTON = InlineKeyboardButton.builder().text("Основное меню")
            .callbackData(QueryCallback.MAIN_MENU.getData()).build();
    private final static InlineKeyboardButton STOP_TIMER_BUTTON = InlineKeyboardButton.builder().text("Остановить таймер")
            .callbackData(QueryCallback.STOP_TIMER.getData()).build();
    private final static InlineKeyboardButton ONE_HOUR = InlineKeyboardButton.builder().text("Напомнить через час")
            .callbackData(QueryCallback.ONE_HOUR.getData()).build();

    public final static InlineKeyboardMarkup MAIN_MENU = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(ONE_MIN_BUTTON, THIRTY_SEC_BUTTON)).keyboardRow(List.of(LIST_OF_TIMERS_BUTTON)).build();
    public final static InlineKeyboardMarkup SUCCESS_TIMER_MENU = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(MAIN_MENU_BUTTON, LIST_OF_TIMERS_BUTTON)).build();
    public final static InlineKeyboardMarkup LIST_TIMERS_MENU = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(STOP_TIMER_BUTTON)).keyboardRow(List.of(MAIN_MENU_BUTTON)).build();
    public final static InlineKeyboardMarkup TO_MAIN_MENU = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(MAIN_MENU_BUTTON)).build();
    public final static InlineKeyboardMarkup TIMER_WAS_DELETED = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(MAIN_MENU_BUTTON)).keyboardRow(List.of(LIST_OF_TIMERS_BUTTON)).build();
    public final static InlineKeyboardMarkup REMINDER = InlineKeyboardMarkup.builder().keyboardRow(List.of(ONE_HOUR))
            .keyboardRow(List.of(MAIN_MENU_BUTTON)).build();
}
