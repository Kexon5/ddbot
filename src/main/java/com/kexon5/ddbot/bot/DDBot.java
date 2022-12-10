package com.kexon5.ddbot.bot;

import com.kexon5.ddbot.exceptions.IllegalTimeInput;
import com.kexon5.ddbot.util.TimeUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

@Slf4j
@Getter
public class DDBot extends TelegramLongPollingBot {
    private static final int RECONNECT_PAUSE = 10_000;
    private static final int SEC_IN_MIN = 60;
    private final static InlineKeyboardButton ONE_MIN_BUTTON = InlineKeyboardButton.builder().text("1 мин")
            .callbackData("1 min").build();
    private final static InlineKeyboardButton THIRTY_SEC_BUTTON = InlineKeyboardButton.builder().text("30 сек")
            .callbackData("30 sec").build();
    private final static InlineKeyboardMarkup MAIN_KEYBOARD = InlineKeyboardMarkup.builder()
            .keyboardRow(List.of(ONE_MIN_BUTTON, THIRTY_SEC_BUTTON)).build();
    private final static String MAIN_MENU_TEXT = "Выберите время, либо введите самостоятельно, " +
            "используя символы: с - секунды, м - минуты, ч - часы. Пример ввода: 2ч 35м 33с; 35c; 7м 2с";
    private final static String TIMER_OUT_TEXT = "Время таймера вышло";
    private final static String TIMER_STARTED = "Таймер успешно заведен";
    private final static  String WRONG_TIME_INPUT = "Временной промежуток введен в неверном формате. " +
            "Попробуйте еще раз, используя символы: с - секунды, м - минуты, ч - часы. Пример ввода: 2ч 35м 33с; 35c; 7м 2с";

    private final String botUsername;
    private final String botToken;

    public DDBot(TelegramBotsApi telegramBotsApi, String botUsername, String botToken) {
        this.botUsername = botUsername;
        this.botToken = botToken;

        this.botConnect(telegramBotsApi);
    }

    @Override
    public void onUpdateReceived(Update update) {
        Long chatId = null;

        try {
            if (update.hasCallbackQuery()) {
                chatId = update.getCallbackQuery().getMessage().getChatId();

                switch (update.getCallbackQuery().getData()) {
                    case "1 min" -> reminder(chatId, SEC_IN_MIN);
                    case "30 sec" -> reminder(chatId, 30);
                }

                execute(AnswerCallbackQuery.builder()
                        .callbackQueryId(update.getCallbackQuery().getId())
                        .build());
            } else {
                chatId = update.getMessage().getChatId();

                if (!update.getMessage().getText().equals("/start")) {
                    reminder(chatId, TimeUtil.parseStringTimeToIntSec(update.getMessage().getText()));
                }
            }

            execute(SendMessage.builder()
                    .text(MAIN_MENU_TEXT)
                    .chatId(chatId)
                    .replyMarkup(MAIN_KEYBOARD)
                    .build());
        } catch (TelegramApiException e) {
            log.error("Request handle error: ", e);
        } catch (IllegalTimeInput e) {
            try {
                sendMsg(WRONG_TIME_INPUT, chatId);
            } catch (TelegramApiException ex) {
                log.error("Request handle error: ", e);
            }
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

    private void sendMsg(String text, Long chatId) throws TelegramApiException {
        execute(SendMessage.builder()
                .text(text)
                .chatId(chatId)
                .build());
    }

    private void reminder(Long chatId, int periodInSec) throws TelegramApiException {
        sendMsg(TIMER_STARTED, chatId);
        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    sendMsg(TIMER_OUT_TEXT, chatId);
                } catch (TelegramApiException e) {
                    log.error("Request handle error: ", e);
                }
            }
        }, Date.from(Instant.now().plusSeconds(periodInSec)));
    }
}