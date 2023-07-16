package com.kexon5.bot.bot.services.schedule;

import com.kexon5.bot.bot.elements.MenuElement;
import com.kexon5.bot.bot.states.ServiceState;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class ScheduleService extends MenuElement {

    public ScheduleService(ServiceState state,
                           Predicate<Long> accessPredicate) {
        super(state, accessPredicate);
    }

    @Override
    public String getAnswer(long userId, @Nullable String userText) {
        return "Раздел для работы с расписанием";
    }
}
