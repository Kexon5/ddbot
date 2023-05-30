package com.kexon5.ddbot.conf.statemachine;

import com.kexon5.ddbot.bot.services.edithospital.EditHospitalService;
import com.kexon5.ddbot.bot.services.hospital.HospitalService;
import com.kexon5.ddbot.bot.services.mainmenu.MainMenuService;
import com.kexon5.ddbot.repositories.UserRepository;
import com.kexon5.ddbot.services.ScheduleService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@DependsOn("dbContext")
public class ServiceConfiguration {

    @Bean
    @DependsOn({"hospitalsMenu", "editHospitalsMenu"})
    public MainMenuService mainMenu(UserRepository userRepository) {
        return new MainMenuService(userRepository);
    }

    @Bean
    public HospitalService hospitalsMenu() {
        return new HospitalService();
    }

    @Bean
    public EditHospitalService editHospitalsMenu(ScheduleService scheduleService) {
        return new EditHospitalService(scheduleService);
    }

}
