package com.kexon5.ddbot.buttons;

import com.kexon5.ddbot.services.messages.Message;
import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

public class ButtonGenerator {

    @Getter
    private static InlineKeyboardButton mainButton;
    private static final AtomicLong generatorId = new AtomicLong();
    private static final Map<Long, Function<Update, Message>> buttonsMap = new HashMap<>();

    public static InlineKeyboardButton of(String text, Function<Update, Message> function) {
        long id = generatorId.getAndIncrement();
        InlineKeyboardButton newButton = getButton(id, text);
        if (text.startsWith("<--")) {
            mainButton = newButton;
        }
        buttonsMap.put(id, function);
        return newButton;
    }

    public static Message sendMessage(Update update) {
        String data = update.getCallbackQuery().getData();
        return sendMessage(update, data);
    }

    public static Message sendMessage(Update update, String data) {
        return buttonsMap.get(Long.parseLong(data)).apply(update);
    }

    public static InlineKeyboardButton getButton(long id, String text) {
        return InlineKeyboardButton.builder()
                .text(text)
                .callbackData(String.valueOf(id))
                .build();
    }
}
