package com.kexon5.ddbot.conf.statemachine;

import com.kexon5.ddbot.bot.services.mainmenu.MainMenuService;
import com.kexon5.ddbot.repositories.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import static com.kexon5.ddbot.bot.states.ServiceState.MAIN_MENU;

@Configuration
public class MenuConfiguration {

    @Bean
    @DependsOn({"hospitalsMenu", "editHospitalsMenu", "administrationMenu", "scheduleMenu", "actionSwitcher", "serviceSwitcher"})
    public MainMenuService mainMenu(UserRepository userRepository) {
        return new MainMenuService(MAIN_MENU, userRepository);
    }
}
