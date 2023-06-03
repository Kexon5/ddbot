package com.kexon5.ddbot.conf.statemachine;

import com.kexon5.ddbot.bot.services.ServiceState;
import com.kexon5.ddbot.bot.services.administration.AdministrationService;
import com.kexon5.ddbot.bot.services.edithospital.EditHospitalService;
import com.kexon5.ddbot.bot.services.hospital.HospitalService;
import com.kexon5.ddbot.bot.services.mainmenu.MainMenuService;
import com.kexon5.ddbot.bot.services.schedule.ScheduleService;
import com.kexon5.ddbot.repositories.UserRepository;
import com.kexon5.ddbot.services.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.util.Optional;
import java.util.function.Predicate;

import static com.kexon5.ddbot.bot.services.ServiceState.*;

@Configuration
@DependsOn("dbContext")
public class ServiceConfiguration {

    @Autowired
    public RepositoryService repositoryService;

    public Predicate<Long> getAccessPredicate(ServiceState state) {
        return userId -> Optional.ofNullable(repositoryService.getUserByUserId(userId))
                                 .map(state.getAccessPredicate())
                                 .orElse(false);
    }

    @Bean
    @DependsOn({"hospitalsMenu", "editHospitalsMenu", "scheduleMenu", "administrationMenu"})
    public MainMenuService mainMenu(UserRepository userRepository) {
        return new MainMenuService(MAIN_MENU, userRepository);
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

}
