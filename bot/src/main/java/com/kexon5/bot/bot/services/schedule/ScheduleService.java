package com.kexon5.bot.bot.services.schedule;

import com.kexon5.bot.bot.elements.MenuElement;
import com.kexon5.bot.bot.states.ServiceState;

import javax.annotation.Nullable;

public class ScheduleService extends MenuElement {

    public ScheduleService(ServiceState state) {
        super(state);
    }

    @Override
    public String getAnswer(long userId, @Nullable String userText) {
        return "Раздел для работы с расписанием";
    }
}
