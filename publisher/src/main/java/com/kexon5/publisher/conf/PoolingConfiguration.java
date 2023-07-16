package com.kexon5.publisher.conf;

import com.kexon5.publisher.bot.PoolingPublisherBot;
import com.kexon5.publisher.service.UpdateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.telegram.abilitybots.api.sender.DefaultSender;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Profile("pooling")
@Configuration
public class PoolingConfiguration {
    @Bean
    public TelegramBotsApi telegramBotsApi() throws TelegramApiException {
        return new TelegramBotsApi(DefaultBotSession.class);
    }

    @Bean
    public PoolingPublisherBot publisherBot(@Value("${bot.username}") String botName,
                                            @Value("${bot.token}") String botToken,
                                            TelegramBotsApi telegramBotsApi,
                                            UpdateService updateService) {
        PoolingPublisherBot bot = new PoolingPublisherBot(botToken, botName, updateService);
        bot.botConnect(telegramBotsApi);
        return bot;
    }

    @Bean
    public SilentSender sender(PoolingPublisherBot bot,
                               UpdateService updateService) {
        SilentSender sender = new SilentSender(new DefaultSender(bot));
        updateService.setSender(sender);

        return sender;
    }

}
