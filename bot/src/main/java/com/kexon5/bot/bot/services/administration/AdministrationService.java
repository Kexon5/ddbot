package com.kexon5.bot.bot.services.administration;

import com.kexon5.bot.bot.elements.MenuElement;
import com.kexon5.bot.bot.states.ServiceState;

import javax.annotation.Nullable;

public class AdministrationService extends MenuElement {


    public AdministrationService(ServiceState state) {
        super(state);
    }

    @Override
    public String getAnswer(long userId, @Nullable String userText) {
        return "Административный уголок:)";
    }
}
