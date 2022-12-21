package com.kexon5.ddbot.bot;

import com.kexon5.ddbot.exceptions.IllegalTimeInput;
import com.kexon5.ddbot.util.Reminder;
import com.kexon5.ddbot.util.TimeUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

@Slf4j
@Getter
public class DDBot extends TelegramLongPollingBot {
    private static final int RECONNECT_PAUSE = 10_000;
    private static final int SEC_IN_MIN = 60;
    private static final int SEC_IN_HOUR = 3600;
    public static final int SEC_BEFORE_RESCHEDULE = 60;
    private final static String MAIN_MENU_TEXT = "Выберите время, либо введите самостоятельно, " +
            "используя символы: с - секунды, м - минуты, ч - часы. Пример ввода: 2ч 35м 33с; 35c; 7м 2с";
    private final static String TIMER_OUT_TEXT = """
            ➡ Не забудь взять с собой паспорт
            ➡ Перед сдачей крови не делай что-то
            ➡ Тут мы скинем тебе навигацию больнички
            ➡ Прочая полезная информация, я хз, что тут надо писать, я кровь не сдавал
            """;
    private final static  String WRONG_TIME_INPUT = "Временной промежуток введен в неверном формате, попробуйте еще раз";
    private final static String TIMER_IS_ADDED = "Таймер добавлен";

    private final String botUsername;
    private final String botToken;
    private final Map<Long, List<Reminder>> reminders = new HashMap<>();

    public DDBot(TelegramBotsApi telegramBotsApi, String botUsername, String botToken) {
        this.botUsername = botUsername;
        this.botToken = botToken;

        this.botConnect(telegramBotsApi);
    }

    @Override
    public void onUpdateReceived(Update update) {
        try {
            if (update.hasMessage()) {
                Long chatId = update.getMessage().getChatId();

                if (update.getMessage().isCommand()) {
                    sendMsgWithKeyboard(chatId, MAIN_MENU_TEXT, Keyboards.MAIN_MENU);
                } else {
                    startReminder(chatId, TimeUtil.parseStringTimeToIntSec(update.getMessage().getText()));
                    sendMsgWithKeyboard(chatId, TIMER_IS_ADDED, Keyboards.SUCCESS_TIMER_MENU);
                }
            } else {
                Long chatId = update.getCallbackQuery().getFrom().getId();
                Integer msgId = update.getCallbackQuery().getMessage().getMessageId();
                String data = update.getCallbackQuery().getData();

                if (data.equals(QueryCallback.MAIN_MENU.getData())) {
                    editMsg(chatId, msgId, MAIN_MENU_TEXT, Keyboards.MAIN_MENU);
                } else if (data.equals(QueryCallback.ONE_MIN.getData())) {
                    startReminder(chatId, SEC_IN_MIN);
                    editMsg(chatId, msgId, TIMER_IS_ADDED, Keyboards.SUCCESS_TIMER_MENU);
                } else if (data.equals(QueryCallback.THIRTY_SEC.getData())) {
                    startReminder(chatId, 30);
                    editMsg(chatId, msgId, TIMER_IS_ADDED, Keyboards.SUCCESS_TIMER_MENU);
                } else if (data.equals(QueryCallback.ONE_HOUR.getData())) {
                    startReminder(chatId, SEC_IN_HOUR);
                    editMsg(chatId, msgId, TIMER_IS_ADDED, Keyboards.SUCCESS_TIMER_MENU);
                } else if (data.equals(QueryCallback.LIST_OF_REMINDERS.getData())) {
                    StringBuilder text = new StringBuilder();

                    if (reminders.get(chatId) == null || reminders.get(chatId).isEmpty()) {
                        text.append("Нет активных таймеров");
                        editMsg(chatId, msgId, text.toString(), Keyboards.TO_MAIN_MENU);
                    } else {
                        text.append("Список активных таймеров:");
                        for (int i = 0; i < reminders.get(chatId).size(); i++) {
                            text.append(String.format("%n%d. Таймер на %s", i + 1
                                    , reminders.get(chatId).get(i).toString()));
                        }
                        editMsg(chatId, msgId, text.toString(), Keyboards.LIST_TIMERS_MENU);
                    }
                } else if (data.equals(QueryCallback.STOP_TIMER.getData())) {
                    InlineKeyboardMarkup.InlineKeyboardMarkupBuilder markupBuilder = InlineKeyboardMarkup.builder();

                    for (int i = 0; i < reminders.get(chatId).size(); i++) {
                        markupBuilder.keyboardRow(List.of(InlineKeyboardButton.builder()
                                .text("Остановить таймер №" + (i + 1)).callbackData(String.valueOf(i)).build()));
                    }
                    execute(EditMessageReplyMarkup.builder().chatId(chatId).messageId(msgId)
                            .replyMarkup(markupBuilder.build()).build());
                } else {
                    stopReminder(chatId, Integer.parseInt(data));
                    editMsg(chatId, msgId, "Таймер удален", Keyboards.TIMER_WAS_DELETED);
                }

                execute(AnswerCallbackQuery.builder().callbackQueryId(update.getCallbackQuery().getId()).build());
            }
        } catch (TelegramApiException e) {
            log.error("Request handle error: ", e);
        } catch (IllegalTimeInput e) {
            try {
                sendMsgWithKeyboard(update.getMessage().getChatId(), WRONG_TIME_INPUT, Keyboards.MAIN_MENU);
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
        execute(SendMessage.builder().text(text).chatId(chatId).build());
    }

    private void sendMsgWithKeyboard(Long chatId, String text, ReplyKeyboard keyboard) throws TelegramApiException {
        execute(SendMessage.builder().chatId(chatId).text(text).replyMarkup(keyboard).build());
    }

    private void editMsg (Long chatId, Integer msgId, String text, InlineKeyboardMarkup keyboard) throws TelegramApiException {
        execute(EditMessageText.builder().chatId(chatId).messageId(msgId).text(text).build());
        execute(EditMessageReplyMarkup.builder().chatId(chatId).messageId(msgId).replyMarkup(keyboard).build());
    }

    private void startReminder(Long chatId, int timePeriod) {
        Date execDate = Date.from(Instant.now().plusSeconds(timePeriod));
        Reminder reminder = new Reminder(execDate);
        List<Reminder> reminderList = reminders.get(chatId);

        reminder.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    sendMsgWithKeyboard(chatId, TIMER_OUT_TEXT, Keyboards.REMINDER);
                    reminders.get(chatId).remove(reminder);
                } catch (TelegramApiException e) {
                    log.error("Request handle error: ", e);
                }
            }
        }, execDate);

        if (reminderList == null) {
            reminderList = new ArrayList<>();
        }
        reminderList.add(reminder);
        reminders.put(chatId, reminderList);
    }

    private void stopReminder(Long chatId, int timerId) {
        reminders.get(chatId).get(timerId).cancel();
        reminders.get(chatId).remove(timerId);
    }
}