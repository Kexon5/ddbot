package com.kexon5.bot.conf.statemachine;

import com.kexon5.bot.bot.services.administration.ActionSwitcher;
import com.kexon5.bot.bot.services.administration.AdministrationService;
import com.kexon5.bot.bot.services.administration.ServiceSwitcher;
import com.kexon5.bot.bot.services.edithospital.EditHospitalService;
import com.kexon5.bot.bot.services.hospital.HospitalService;
import com.kexon5.bot.bot.services.schedule.ScheduleService;
import com.kexon5.bot.bot.states.ServiceState;
import com.kexon5.bot.models.ElementSetting;
import com.kexon5.bot.repositories.ElementSettingRepository;
import com.kexon5.bot.services.RepositoryService;
import com.kexon5.bot.statemachine.ButtonReply;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.kexon5.bot.bot.states.ServiceState.*;

@Configuration
@DependsOn("dbContext")
public class ServiceConfiguration {

    @Autowired
    public RepositoryService repositoryService;
    @Autowired
    public ElementSettingRepository settingRepository;

    public Predicate<Long> getAccessPredicate(ServiceState state) {
        if (!settingRepository.existsByElementName(state.name())) {
            settingRepository.createNew(state.name(), ElementSetting.Type.SERVICE);
        }

        Predicate<Long> userAccessCheck =  userId -> Optional.ofNullable(repositoryService.getUserByUserId(userId))
                                                             .map(state.getAccessPredicate())
                                                             .orElse(false);

        Predicate<Long> stateWorkingCheck = userId -> settingRepository.isWorking(state.name(), ElementSetting.Type.SERVICE);

        return userAccessCheck.and(stateWorkingCheck);
    }

    public <T extends Enum> List<ButtonReply.ButtonReplyBuilder> getBuilders(List<ElementSetting> elementSettings, T element) {
        AtomicInteger counter = new AtomicInteger();

        return elementSettings.stream()
                              .map(elementSetting ->
                                      ButtonReply.builder(
                                                     counter.getAndIncrement() + element.name(),
                                                     elementSetting.getElementName(),
                                                     elementSetting.isWorking()
                                                 )
                                                 .buttonChange(newState -> settingRepository.save(elementSetting.inverseWorking()))
                              )
                              .toList();
    }

    @Bean
    public HospitalService hospitalsMenu() {
        return new HospitalService(HOSPITALS_MENU, getAccessPredicate(HOSPITALS_MENU));
    }

    @Bean
    public EditHospitalService editHospitalsMenu(RepositoryService repositoryService) {
        return new EditHospitalService(
                EDIT_HOSPITALS_MENU,
                getAccessPredicate(EDIT_HOSPITALS_MENU),
                repositoryService
        );
    }

    @Bean
    public AdministrationService administrationMenu() {
        return new AdministrationService(ADMINISTRATION_MENU, getAccessPredicate(ADMINISTRATION_MENU));
    }

    @Bean
    public ScheduleService scheduleMenu() {
        return new ScheduleService(SCHEDULE_MENU, getAccessPredicate(SCHEDULE_MENU));
    }

    @Bean
    public ServiceSwitcher serviceSwitcher() {
        Set<String> removeSet = Set.of(MAIN_MENU, ADMINISTRATION_MENU, SERVICE_SWITCHER_MENU, ACTION_SWITCHER_MENU).stream()
                                   .map(Enum::name)
                                   .collect(Collectors.toSet());

        List<ElementSetting> serviceSettings = settingRepository.findAllByType(ElementSetting.Type.SERVICE);

        serviceSettings.removeIf(element -> removeSet.contains(element.getElementName()));

        return new ServiceSwitcher(
                SERVICE_SWITCHER_MENU,
                getAccessPredicate(SERVICE_SWITCHER_MENU),
                getBuilders(serviceSettings, SERVICE_SWITCHER_MENU)
        );
    }

    @Bean
    public ActionSwitcher actionSwitcher() {
        List<ElementSetting> actionSettings = settingRepository.findAllByType(ElementSetting.Type.ACTION);

        return new ActionSwitcher(
                ACTION_SWITCHER_MENU,
                getAccessPredicate(ACTION_SWITCHER_MENU),
                getBuilders(actionSettings, ACTION_SWITCHER_MENU)
        );
    }

}
