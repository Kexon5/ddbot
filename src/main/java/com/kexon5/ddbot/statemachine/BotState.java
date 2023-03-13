package com.kexon5.ddbot.statemachine;

import com.kexon5.ddbot.utils.BotMessage;
import org.springframework.messaging.Message;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Objects;

import static com.kexon5.ddbot.utils.Utils.*;

public interface BotState extends Eventable {
    String name();

    static BotState[] values() {
        return null;
    }

    default BotMessage getFinalMessage(Message<Integer> userMsg) {
        return getMessage(Objects.requireNonNull(userMsg.getHeaders().get(ID, Long.class)), 0, "", true);
    }

    default BotMessage getMessage(Message<Integer> userMsg) {
        return getMessage(
                Objects.requireNonNull(userMsg.getHeaders().get(ID, Long.class)),
                Objects.requireNonNull(userMsg.getHeaders().get(MSG_ID, Integer.class)),
                userMsg.getHeaders().get(TEXT, String.class),
                Objects.requireNonNull(userMsg.getHeaders().get(IS_MSG, Boolean.class))
        );
    }

    default BotMessage getMessage(long userId,
                                  int msgId,
                                  @Nullable String userText,
                                  boolean isMsg) {
        init(userId);
        String msgAnswer = getMessageAnswer(userText);
        if (msgAnswer != null) {
            SendMessage.SendMessageBuilder builder = SendMessage.builder()
                    .chatId(userId)
                    .text(msgAnswer)
                    .parseMode("Markdown");

            setOptionsToBuilder(builder);
            return new BotMessage(builder.build());
        }
        return new BotMessage(Collections.emptyList());
    }

    default void init(long userId) {}

    default String getMessageAnswer(String userText) {
        return null;
    }

    default void setOptionsToBuilder(SendMessage.SendMessageBuilder builder) {}
}
