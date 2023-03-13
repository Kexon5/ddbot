package com.kexon5.ddbot.conf;

import com.kexon5.ddbot.bot.DDBot;
import com.kexon5.ddbot.statemachine.BotState;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.StateMachine;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Configuration
public class BotConfiguration  {

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

    @Bean(initMethod = "init")
    public DDBot ddBot(TelegramBotsApi telegramBotsApi,
                       StateMachine<BotState, Integer> stateMachine,

                       @Value("${bot.username}") String botName,
                       @Value("${bot.token}") String botToken) {
        DDBot bot = new DDBot(stateMachine, botName, botToken);
        bot.botConnect(telegramBotsApi);
        return bot;
    }

}
