package com.kexon5.ddbot.utils;

import org.springframework.data.util.Pair;
import org.springframework.messaging.support.GenericMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Utils {

    public static final String ID = "USER_ID";
    public static final String MSG_ID = "MSG_ID";
    public static final String IS_MSG = "IS_MSG";
    public static final String TEXT = "TEXT";

    public static final String EXIT = "EXIT";

    public static ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder getReplyKeyboardMarkupBuilder(List<KeyboardButton> buttons) {
        return ReplyKeyboardMarkup.builder()
                .keyboard(buttons.stream().map(b -> new KeyboardRow(List.of(b))).toList())
                .resizeKeyboard(true);
    }

    public static InlineKeyboardMarkup getMenu(Collection<InlineKeyboardButton> buttons) {
        List<List<InlineKeyboardButton>> keyboard = new ArrayList<>();
        buttons.forEach(button -> keyboard.add(List.of(button)));

        return InlineKeyboardMarkup.builder().keyboard(keyboard).build();
    }

    public static org.springframework.messaging.Message<Integer> getMessageFromUpdate(Update update) {
        return getMessageFromUpdate(update, 0);
    }

    public static org.springframework.messaging.Message<Integer> getMessageFromUpdate(Update update, int msgPayload) {
        if (update.hasMessage()) {
            Message msg = update.getMessage();

            return new GenericMessage<>(msgPayload, Map.of(ID, msg.getFrom().getId(), TEXT, msg.getText(), MSG_ID, msg.getMessageId(), IS_MSG, true));
        } else if (update.hasCallbackQuery()) {
            CallbackQuery cq = update.getCallbackQuery();
            return new GenericMessage<>(Integer.parseInt(cq.getData()), Map.of(ID, cq.getFrom().getId(),  MSG_ID, cq.getMessage().getMessageId(), IS_MSG, false));
        }
        return null;
    }

    public static long getDaysBetween(LocalDate first, LocalDate second) {
        return Duration.between(first.atStartOfDay(), second.atStartOfDay()).toDays();
    }

    public static InlineKeyboardButton buttonFrom(Pair<Integer, String> button) {
        return getButton(button.getFirst(), button.getSecond());
    }

    public static InlineKeyboardButton buttonFrom(Integer eventCode, String text) {
        return getButton(eventCode, text);
    }

    public static InlineKeyboardButton getButton(int id, String text) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(String.valueOf(id))
                .build();
    }
}
