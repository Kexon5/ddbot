package com.kexon5.bot.bot.services.edithospital;

import com.kexon5.bot.bot.elements.MenuElement;
import com.kexon5.bot.bot.states.ServiceState;
import com.kexon5.bot.models.hospital.Hospital;
import com.kexon5.bot.services.RepositoryService;
import com.kexon5.bot.utils.markup.BoldString;
import com.kexon5.bot.utils.markup.MarkupList;

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
