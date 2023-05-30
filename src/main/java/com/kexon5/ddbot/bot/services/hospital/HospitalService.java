package com.kexon5.ddbot.bot.services.hospital;

import com.google.common.collect.ImmutableList;
import com.kexon5.ddbot.bot.services.ServiceElement;
import com.kexon5.ddbot.bot.services.ServiceState;
import com.kexon5.ddbot.utils.markup.MarkupList;

import javax.annotation.Nullable;

public class HospitalService extends ServiceElement {

    private static final MarkupList<String> actionDetails = new MarkupList<>(
            new ImmutableList.Builder<String>()
                    .add("Отредактировать информацию об ОПК")
                    .add("Получить ссылку на таблицу с расписанием")
                    .add("Импортировать записи из таблицы")
                    .build()
    );

    public HospitalService() {
        super(ServiceState.HOSPITALS_MENU);
    }

    @Override
    public String getAnswer(long userId, @Nullable String userText) {
        return "В данном разделе Вы можете:\n" + actionDetails;
    }
}
