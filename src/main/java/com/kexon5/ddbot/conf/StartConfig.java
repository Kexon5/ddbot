package com.kexon5.ddbot.conf;

import com.kexon5.ddbot.bot.DDBot;
import com.kexon5.ddbot.services.MainMenuService;
import com.kexon5.ddbot.services.hospitals.HospitalService;
import com.kexon5.ddbot.services.hospitals.actions.AddHospital;
import com.kexon5.ddbot.services.hospitals.actions.RemoveHospital;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.List;

@Configuration
public class StartConfig {
    @Value("${bot.username}")
    private String botName;
    @Value("${bot.token}")
    private String botToken;

    @Bean
    public HospitalService hospitalService() {
        return new HospitalService();
    }

    @Bean
    public MainMenuService menuService(HospitalService hospitalService) {
        return new MainMenuService(List.of(hospitalService), List.of());
    }

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

    @Bean
    public DDBot ddBot(TelegramBotsApi telegramBotsApi, MainMenuService mainMenuService) {
        DDBot bot = new DDBot(mainMenuService, botName, botToken);
        bot.botConnect(telegramBotsApi);
        return bot;
    }

}
