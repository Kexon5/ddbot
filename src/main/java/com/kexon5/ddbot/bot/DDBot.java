package com.kexon5.ddbot.bot;

import com.kexon5.ddbot.buttons.ButtonGenerator;
import com.kexon5.ddbot.services.MainMenuService;
import com.kexon5.ddbot.services.messages.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

@Slf4j
@Getter
@RequiredArgsConstructor
public class DDBot extends TelegramLongPollingBot {
    private static final int RECONNECT_PAUSE = 10_000;
    private final int updateCounter = 0;
    private String lastCallback = "";

    private final MainMenuService mainMenuService;
    private final String botUsername;
    private final String botToken;

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasCallbackQuery()) {
                lastCallback = update.getCallbackQuery().getData();
                execute(ButtonGenerator.sendMessage(update));
            } else {
                if (!lastCallback.isEmpty()) {
                    Message msg = ButtonGenerator.sendMessage(update, lastCallback);
                    execute(msg);
                    if (!msg.isLastMsg()) {
                        return;
                    }
                }
                lastCallback = "";
                execute(mainMenuService.getMenu(update));
            }
        } catch (TelegramApiException e) {
            log.error("Request handle error: ", e);
        }
    }


    public void execute(Message msg) throws TelegramApiException {
        for (var m: msg.msgList()) {
            execute(m);
        }
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
}