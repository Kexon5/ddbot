package com.kexon5.bot.bot.services.account;

import com.kexon5.bot.bot.elements.MenuElement;
import com.kexon5.bot.bot.states.ServiceState;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class AccountService extends MenuElement {

    public AccountService(ServiceState state,
                           Predicate<Long> accessPredicate) {
        super(state, accessPredicate);
    }

    @Override
    public String getAnswer(long userId, @Nullable String userText) {
        return "Ваш уголочек:З";
    }
}