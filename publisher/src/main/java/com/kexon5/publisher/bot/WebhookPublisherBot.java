package com.kexon5.publisher.bot;


import com.kexon5.publisher.service.UpdateService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramWebhookBot;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

@Slf4j
@Getter
public class WebhookPublisherBot extends TelegramWebhookBot {

    private final String botUsername;
    private final UpdateService updateService;

    public WebhookPublisherBot(String botToken,
                               String botUsername,
                               UpdateService updateService) {
        super(botToken);

        this.botUsername = botUsername;
        this.updateService = updateService;
    }


    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        updateService.addUpdate(update);
        return null;
    }

    @Override
    public String getBotPath() {
        return "";
    }

}
