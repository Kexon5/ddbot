package com.kexon5.ddbot.conf;

import com.kexon5.ddbot.bot.DDBot;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class StartConfig {
    @Value("${bot.username}")
    private String botName;
    @Value("${bot.token}")
    private String botToken;

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

    @Bean
    public DDBot ddBot(TelegramBotsApi telegramBotsApi) {
        return new DDBot(telegramBotsApi, botName, botToken);
    }

}
