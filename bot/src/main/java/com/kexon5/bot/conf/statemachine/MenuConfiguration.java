package com.kexon5.bot.conf.statemachine;

import com.kexon5.bot.bot.services.mainmenu.MainMenuService;
import com.kexon5.common.repositories.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import static com.kexon5.bot.bot.states.ServiceState.MAIN_MENU;

@Configuration
public class MenuConfiguration {

    @Bean
    @DependsOn({"hospitalsMenu", "editHospitalsMenu", "administrationMenu", "scheduleMenu", "actionSwitcher", "serviceSwitcher"})
    public MainMenuService mainMenu(UserRepository userRepository) {
        return new MainMenuService(MAIN_MENU, userRepository);
    }
}
