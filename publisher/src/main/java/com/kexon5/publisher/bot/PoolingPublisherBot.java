package com.kexon5.publisher.bot;


import com.kexon5.publisher.service.UpdateService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.List;

@Slf4j
@Getter
public class PoolingPublisherBot extends TelegramLongPollingBot {

    private static final int RECONNECT_PAUSE = 10_000;

    private final String botUsername;
    private final UpdateService updateService;


    public PoolingPublisherBot(String botToken,
                               String botUsername,
                               UpdateService updateService) {
        super(botToken);

        this.botUsername = botUsername;
        this.updateService = updateService;
    }

    public void botConnect(TelegramBotsApi telegramBotsApi) {
        try {
            telegramBotsApi.registerBot(this);
            log.info("Bot registered!");
        } catch (TelegramApiException e) {
            log.error("Try to reconnect after " + RECONNECT_PAUSE / 1000 + "sec. Error: " + e.getMessage());
            try {
                Thread.sleep(RECONNECT_PAUSE);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                return;
            }
            botConnect(telegramBotsApi);
        } catch (Exception e) {
            log.error("Unknown error: ", e);
        }
    }


    @Override
    public void onUpdateReceived(Update update) {}

    @Override
    public void onUpdatesReceived(List<Update> updates) {
        updateService.addUpdates(updates);
    }
}
