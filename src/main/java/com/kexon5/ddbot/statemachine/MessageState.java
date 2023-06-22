package com.kexon5.ddbot.statemachine;

import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.annotation.Nullable;
import java.io.Serializable;

import static org.telegram.abilitybots.api.objects.Flag.CALLBACK_QUERY;
import static org.telegram.abilitybots.api.objects.Flag.MESSAGE;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

public interface MessageState {

    default BotApiMethod<? extends Serializable> getMessage(Update update) {
        return getMessage(
                getChatId(update),
                CALLBACK_QUERY.test(update)
                        ? update.getCallbackQuery().getMessage().getMessageId()
                        : null,
                MESSAGE.test(update)
                        ? update.getMessage().getText()
                        : null
        );
    }

    default BotApiMethod<? extends Serializable> getMessage(Long userId, Integer msgId, @Nullable String userText) {
        String answer = getAnswer(userId, userText);
        if (answer != null) {
            SendMessage.SendMessageBuilder builder = SendMessage.builder()
                                                                .chatId(userId)
                                                                .text(answer)
                                                                .parseMode("Markdown");
            setOptionsToBuilder(userId, builder);
            return builder.build();
        }

        return null;
    }


    default String getAnswer(long userId, @Nullable String userText) { return null; }

    default void setOptionsToBuilder(long userId, SendMessage.SendMessageBuilder builder) {}
}
