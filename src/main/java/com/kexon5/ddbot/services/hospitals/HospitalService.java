package com.kexon5.ddbot.services.hospitals;

import com.kexon5.ddbot.markup.BoldString;
import com.kexon5.ddbot.markup.MarkupList;
import com.kexon5.ddbot.models.Location;
import com.kexon5.ddbot.services.AbstractService;
import com.kexon5.ddbot.services.hospitals.actions.AddHospital;
import com.kexon5.ddbot.services.hospitals.actions.RemoveHospital;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HospitalService extends AbstractService {

    private static final List<Location> hospitals = new ArrayList<>();

    public HospitalService() {
        super(Collections.emptyList(), List.of(new AddHospital(hospitals), new RemoveHospital(hospitals)));
    }

    @Override
    public String getMainMessage(Update update) {
        List<String> hospitalsList = new MarkupList<>(hospitals.stream().map(Location::getPlaceName).toList());
        return String.valueOf(new BoldString("Текущий список больниц:\n")) + hospitalsList;
    }

    @Override
    public String getButtonText() {
        return "Настройка больничек";
    }
}