package com.kexon5.ddbot.bot;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Getter
public class DDBot extends TelegramLongPollingBot {
    private static final int RECONNECT_PAUSE = 10_000;
    private final String botUsername;
    private final String botToken;

    public DDBot(TelegramBotsApi telegramBotsApi, String botUsername, String botToken) {
        this.botUsername = botUsername;
        this.botToken = botToken;

        this.botConnect(telegramBotsApi);
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            SendMessage msg = new SendMessage();

            msg.setChatId(update.getMessage().getChatId());
            msg.setText(update.getMessage().getText());
            execute(msg);
        } catch (TelegramApiException e) {
            log.error("Request handle error: ", e);
        }
    }

    private void botConnect(TelegramBotsApi telegramBotsApi) {
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
}