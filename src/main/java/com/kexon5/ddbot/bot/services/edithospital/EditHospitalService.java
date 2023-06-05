package com.kexon5.ddbot.bot.services.edithospital;

import com.kexon5.ddbot.bot.services.MenuElement;
import com.kexon5.ddbot.bot.services.ServiceState;
import com.kexon5.ddbot.models.hospital.Hospital;
import com.kexon5.ddbot.services.RepositoryService;
import com.kexon5.ddbot.utils.markup.BoldString;
import com.kexon5.ddbot.utils.markup.MarkupList;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class EditHospitalService extends MenuElement {

    private final RepositoryService repositoryService;

    public EditHospitalService(ServiceState state,
                               Predicate<Long> accessPredicate,
                               RepositoryService repositoryService) {
        super(state, accessPredicate);

        this.repositoryService = repositoryService;
    }

    @Override
    public String getAnswer(long userId, @Nullable String userText) {
        List<String> hospitalsList = new MarkupList<>(repositoryService.getAllHospitals().stream()
                                                                       .map(Hospital::getName)
                                                                       .toList());

        return String.valueOf(new BoldString("Текущий список больниц:\n\n")) + hospitalsList;
    }
}
