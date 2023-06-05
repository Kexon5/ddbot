package com.kexon5.ddbot.bot.services.administration;

import com.kexon5.ddbot.bot.services.MenuElement;
import com.kexon5.ddbot.bot.services.ServiceState;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class AdministrationService extends MenuElement {


    public AdministrationService(ServiceState state,
                                 Predicate<Long> accessPredicate) {
        super(state, accessPredicate);
    }

    @Override
    public String getAnswer(long userId, @Nullable String userText) {
        return "Административный уголок:)";
    }
}
