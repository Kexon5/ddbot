package com.kexon5.bot.bot.services.administration;

import com.kexon5.bot.bot.elements.InteractiveMenuElement;
import com.kexon5.bot.bot.states.ServiceState;
import com.kexon5.bot.statemachine.ButtonReply;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class ActionSwitcher extends InteractiveMenuElement {
    public ActionSwitcher(ServiceState serviceState,
                           @Nullable Predicate<Long> accessPredicate,
                           List<ButtonReply.ButtonReplyBuilder> buttonReplies) {
        super(serviceState, accessPredicate, buttonReplies);
    }

    @Override
    public String getAnswer(long userId, @Nullable String userText) {
        return "Включить/отключить действия";
    }
}