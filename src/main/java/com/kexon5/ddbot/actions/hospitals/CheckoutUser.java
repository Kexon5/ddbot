package com.kexon5.ddbot.actions.hospitals;

import com.kexon5.ddbot.models.hospital.HospitalRecord;
import com.kexon5.ddbot.services.ScheduleService;
import com.kexon5.ddbot.statemachine.BotState;
import com.kexon5.ddbot.statemachine.Eventable;
import com.kexon5.ddbot.utils.Utils;
import com.kexon5.ddbot.utils.markup.BoldString;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

import java.util.List;

public class CheckoutUser {
    @RequiredArgsConstructor
    public enum CheckoutSteps implements BotState, Eventable {
        STEP1 {
            @Override
            public void init(long userId) {
                answer = scheduleService.getUserActiveRecord(userId);
            }

            @Override
            public String getMessageAnswer(String userText) {
                return "У Вас есть активная запись: " +
                        new BoldString(answer.toCommonString()) +
                        "\n\nВы действительно хотите отменить запись?";
            }

            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder) {
                builder.replyMarkup(MENU_BUILDER.build());
            }

            @Override
            public boolean validate(String text) {
                needToRemoveRecord = text.equals(YES);
                return needToRemoveRecord || text.equals(NO);
            }

            @Override
            public void action(long userId, String userText) {
                if (needToRemoveRecord) {
                    answer.removeUser(userId);
                    scheduleService.saveRecord(answer);
                }
            }
        },
        STEP2 {

            @Override
            public String getMessageAnswer(String userText) {
                return needToRemoveRecord
                        ? "Ваша запись успешно удалена"
                        : "Успешно ничего не сделано";
            }

            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder) {
                builder.replyMarkup(new ReplyKeyboardRemove(true));
            }
        };

        private static HospitalRecord answer;

        private static ScheduleService scheduleService;

        private static final String YES = "Да";
        private static final String NO = "Нет";

        private static boolean needToRemoveRecord = false;

        private static final ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder MENU_BUILDER =
                Utils.getReplyKeyboardMarkupBuilder(List.of(new KeyboardButton(YES), new KeyboardButton(NO)));

    }

    public CheckoutUser(ScheduleService scheduleService) {
        CheckoutSteps.scheduleService = scheduleService;
    }
}
