package com.kexon5.bot.bot.services.account;

import com.kexon5.bot.bot.elements.MenuElement;
import com.kexon5.bot.bot.states.ServiceState;

import javax.annotation.Nullable;

public class AccountService extends MenuElement {

    public AccountService(ServiceState state) {
        super(state);
    }

    @Override
    public String getAnswer(long userId, @Nullable String userText) {
        return "Ваш уголочек:З";
    }
}