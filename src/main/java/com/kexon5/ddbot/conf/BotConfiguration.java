package com.kexon5.ddbot.conf;

import com.kexon5.ddbot.bot.DDBot;
import com.kexon5.ddbot.bot.services.mainmenu.MainMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.abilitybots.api.objects.ReplyCollection;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Configuration
public class BotConfiguration  {

    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

    @Bean
    public DDBot ddBot(@Value("${bot.username}") String botName,
                       @Value("${bot.token}") String botToken,
                       TelegramBotsApi telegramBotsApi,
                       ReplyCollection actionReplyCollection,
                       MainMenuService mainMenu) {
        DDBot bot = new DDBot(botToken, botName, actionReplyCollection, mainMenu);
        bot.botConnect(telegramBotsApi);
        return bot;
    }

}
