package com.kexon5.ddbot.bot.services.schedule;

import com.kexon5.ddbot.bot.services.ServiceElement;
import com.kexon5.ddbot.bot.services.ServiceState;

import javax.annotation.Nullable;
import java.util.function.Predicate;

public class ScheduleService extends ServiceElement {

    public ScheduleService(ServiceState state,
                           Predicate<Long> accessPredicate) {
        super(state, accessPredicate);
    }

    @Override
    public String getAnswer(long userId, @Nullable String userText) {
        return "Раздел для работы с расписанием";
    }
}
