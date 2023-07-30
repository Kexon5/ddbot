package com.kexon5.bot.bot.elements;

import com.google.common.collect.ImmutableList;
import com.kexon5.bot.bot.states.ServiceState;
import com.kexon5.bot.utils.Utils;
import com.kexon5.common.statemachine.ButtonReply;
import com.kexon5.common.statemachine.DialogueFlow;
import com.kexon5.common.statemachine.InteractiveButtonFactory;
import org.jetbrains.annotations.Nullable;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.Serializable;
import java.util.List;

public abstract class InteractiveMenuElement extends AbstractServiceElement {
    private final List<ButtonReply> buttonReplies;
    private final InteractiveButtonFactory buttonFactory;


    public InteractiveMenuElement(ServiceState serviceState,
                                  List<ButtonReply.ButtonReplyBuilder> buttonReplies,
                                  InteractiveButtonFactory buttonFactory) {
        super(serviceState);

        this.buttonReplies = buttonReplies.stream()
                                          .map(buttonReplyBuilder -> buttonReplyBuilder
                                                  .action((bot, upd) -> bot.silent().executeAsync(getMessage(upd), consumer -> {}))
                                                  .build())
                                          .toList();

        this.buttonFactory = buttonFactory;
    }

    @Override
    public DialogueFlow.DialogueFlowBuilder createReplyFlow() {
        DialogueFlow.DialogueFlowBuilder builder = DialogueFlow.builder(dbContext)
                                                               .enableStats(elementState.name())
                                                               .actionService(this::getMessage);

        buttonReplies.forEach(builder::next);

        return builder;
    }

    @Override
    public BotApiMethod<? extends Serializable> getMessage(long userId, Integer msgId, @Nullable String userText) {
        return editMessage(userId, msgId);
    }

    private List<InlineKeyboardButton> getInteractiveButtons(long userId) {
        return buttonFactory.apply(userId, buttonReplies.stream()
                                                     .map(Reply::name)
                                                     .toList());
    }

    @Override
    public InlineKeyboardMarkup getFilteredMenu(long userId) {
        return Utils.getMenu(ImmutableList.<InlineKeyboardButton>builder()
                                          .addAll(getInteractiveButtons(userId))
                                          .addAll(getButtons(userId))
                                          .build()
        );
    }

}
