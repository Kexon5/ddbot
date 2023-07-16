package com.kexon5.publisher.conf;

import com.kexon5.publisher.bot.WebhookPublisherBot;
import com.kexon5.publisher.service.UpdateService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.telegram.abilitybots.api.sender.DefaultSender;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.util.List;

@Profile("webhook")
@Configuration
public class WebhookConfiguration {

    @Bean
    public InputFile certificate(@Value("${bot.pub.path}") String certPath) {
        return new InputFile(new File(certPath));
    }

    @Bean
    public SetWebhook setWebhook(InputFile certificate,
                                 @Value("${publisher.address}") String url) {
        SetWebhook webhook = new SetWebhook();
        webhook.setUrl(url);
        webhook.setCertificate(certificate);
        webhook.setAllowedUpdates(List.of("message", "callback_query"));
        webhook.setDropPendingUpdates(true);
        return webhook;
    }

    @Bean
    public WebhookPublisherBot publisherBot(@Value("${bot.username}") String botName,
                                            @Value("${bot.token}") String botToken,
                                            SetWebhook setWebhook,
                                            UpdateService updateService) throws TelegramApiException {
        WebhookPublisherBot bot = new WebhookPublisherBot(botToken, botName, updateService);
        bot.setWebhook(setWebhook);
        return bot;
    }

    @Bean
    public SilentSender sender(WebhookPublisherBot bot,
                               UpdateService updateService) {
        SilentSender sender = new SilentSender(new DefaultSender(bot));
        updateService.setSender(sender);

        return sender;
    }

}
