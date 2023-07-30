package com.kexon5.bot.bot.services.hospital;

import com.google.common.collect.ImmutableList;
import com.kexon5.bot.bot.elements.MenuElement;
import com.kexon5.bot.bot.states.ServiceState;
import com.kexon5.bot.utils.markup.MarkupList;

import javax.annotation.Nullable;

public class HospitalService extends MenuElement {

    private static final MarkupList<String> actionDetails = new MarkupList<>(
            new ImmutableList.Builder<String>()
                    .add("Отредактировать информацию об ОПК")
                    .add("Получить ссылку на таблицу с расписанием")
                    .add("Импортировать записи из таблицы")
                    .build()
    );

    public HospitalService(ServiceState state) {
        super(state);
    }

    @Override
    public String getAnswer(long userId, @Nullable String userText) {
        return "В данном разделе Вы можете:\n" + actionDetails;
    }
}
