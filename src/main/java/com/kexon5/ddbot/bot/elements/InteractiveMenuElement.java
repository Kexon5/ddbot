package com.kexon5.ddbot.bot.elements;

import com.kexon5.ddbot.bot.states.ServiceState;
import com.kexon5.ddbot.statemachine.ButtonReply;
import com.kexon5.ddbot.statemachine.DialogueFlow;
import org.jetbrains.annotations.Nullable;
import org.springframework.data.util.Pair;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;

import java.io.Serializable;
import java.util.List;
import java.util.function.Predicate;

public abstract class InteractiveMenuElement extends AbstractServiceElement {
    private final List<ButtonReply> buttonReplies;

    public InteractiveMenuElement(ServiceState serviceState,
                                  List<ButtonReply.ButtonReplyBuilder> buttonReplies) {
        this(serviceState, null, buttonReplies);
    }

    public InteractiveMenuElement(ServiceState serviceState,
                                  @Nullable Predicate<Long> accessPredicate,
                                  List<ButtonReply.ButtonReplyBuilder> buttonReplies) {
        super(serviceState, accessPredicate);

        this.buttonReplies = buttonReplies.stream()
                                          .map(buttonReplyBuilder -> buttonReplyBuilder
                                                  .action((bot, upd) -> bot.silent().execute(getMessage(upd)))
                                                  .build())
                                          .toList();
    }

    @Override
    public DialogueFlow.DialogueFlowBuilder createReplyFlow() {
        DialogueFlow.DialogueFlowBuilder builder = DialogueFlow.builder(dbContext)
                                                               .enableStats(elementState.name())
                                                               .actionService(this::getMessage);

        for (ButtonReply buttonReply : buttonReplies) {
            builder.next(buttonReply);
            buttons.add(Pair.of(userId -> true, buttonReply.getButton()));
        }

        return builder;
    }

    @Override
    public BotApiMethod<? extends Serializable> getMessage(Long userId, Integer msgId, @Nullable String userText) {
        return editMessage(userId, msgId);
    }

}
