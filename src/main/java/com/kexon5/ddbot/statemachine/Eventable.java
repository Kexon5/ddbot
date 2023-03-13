package com.kexon5.ddbot.statemachine;

import com.kexon5.ddbot.utils.Utils;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.action.Action;
import org.springframework.statemachine.guard.Guard;

import java.util.Objects;
import java.util.Optional;

import static com.kexon5.ddbot.utils.Utils.ID;
import static com.kexon5.ddbot.utils.Utils.TEXT;

public interface Eventable {
    default Guard<BotState, Integer> guard() {
        return context -> {
            try {
                return Optional.ofNullable(context.getMessageHeaders().get(Utils.TEXT, String.class))
                        .filter(this::validate)
                        .isPresent();
            } catch (Exception ex) {
                validateError(ex);
                return false;
            }
        };
    }

    default boolean validate(String text) {
        return true;
    }

    default void validateError(Throwable ex) {}
    default void actionError(Throwable ex) {}

    default Action<BotState, Integer> action() {
        return context -> {
            try {
                MessageHeaders messageHeaders = context.getMessageHeaders();
                action(Objects.requireNonNull(messageHeaders.get(ID, Long.class)),
                        messageHeaders.get(TEXT, String.class));
            } catch (Exception ex) {
                actionError(ex);
            }
        };
    }

    default void action(long userId, String userText) {}
}
