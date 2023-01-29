package com.kexon5.ddbot.services.actions;

import com.kexon5.ddbot.services.messages.MessageCreator;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import static com.kexon5.ddbot.utils.Utils.getId;

public record ActionStep(MessageCreator messageCreator, UnaryOperator<String> validator) {

    public ActionStep(Supplier<String> msg, UnaryOperator<String> validator) {
        this(update -> List.of(SendMessage.builder()
                        .chatId(getId(update))
                        .text(msg.get())
                        .build()),
                validator);
    }

}
