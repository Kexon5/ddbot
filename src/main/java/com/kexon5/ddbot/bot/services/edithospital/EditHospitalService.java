package com.kexon5.ddbot.bot.services.edithospital;

import com.kexon5.ddbot.bot.services.ServiceElement;
import com.kexon5.ddbot.bot.services.ServiceState;
import com.kexon5.ddbot.models.hospital.Hospital;
import com.kexon5.ddbot.services.ScheduleService;
import com.kexon5.ddbot.utils.markup.BoldString;
import com.kexon5.ddbot.utils.markup.MarkupList;

import javax.annotation.Nullable;
import java.util.List;

public class EditHospitalService extends ServiceElement {

    private final ScheduleService scheduleService;

    public EditHospitalService(ScheduleService scheduleService) {
        super(ServiceState.EDIT_HOSPITALS_MENU);

        this.scheduleService = scheduleService;
    }

    @Override
    public String getAnswer(long userId, @Nullable String userText) {
        List<String> hospitalsList = new MarkupList<>(scheduleService.getAllHospitals().stream()
                                                                     .map(Hospital::getName)
                                                                     .toList());

        return String.valueOf(new BoldString("Текущий список больниц:\n\n")) + hospitalsList;
    }
}
