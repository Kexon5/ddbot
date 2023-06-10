package com.kexon5.ddbot.bot.services.administration;

import com.kexon5.ddbot.bot.elements.InteractiveMenuElement;
import com.kexon5.ddbot.bot.states.ServiceState;
import com.kexon5.ddbot.statemachine.ButtonReply;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Predicate;

public class ServiceSwitcher extends InteractiveMenuElement {
    public ServiceSwitcher(ServiceState serviceState,
                           @Nullable Predicate<Long> accessPredicate,
                           List<ButtonReply.ButtonReplyBuilder> buttonReplies) {
        super(serviceState, accessPredicate, buttonReplies);
    }

    @Override
    public String getAnswer(long userId, @Nullable String userText) {
        return "Включить/отключить сервисы";
    }
}
