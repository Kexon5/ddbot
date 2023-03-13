package com.kexon5.ddbot.actions.hospitals;

import com.kexon5.ddbot.models.hospital.HospitalRecord;
import com.kexon5.ddbot.services.ScheduleService;
import com.kexon5.ddbot.statemachine.BotState;
import com.kexon5.ddbot.statemachine.Eventable;
import com.kexon5.ddbot.utils.Utils;
import com.kexon5.ddbot.utils.markup.BoldString;
import com.kexon5.ddbot.utils.markup.MarkupList;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SignupUser {
    @RequiredArgsConstructor
    public enum SignupSteps implements BotState, Eventable {
        PLACE_NAME {
            @Override
            public void init(long userId) {
                recordsForMsg = records.stream()
                        .collect(Collectors.groupingBy(HospitalRecord::getHospital, Collectors.toCollection(ArrayList::new)));
            }

            @Override
            public String getMessageAnswer(String userText) {
                StringBuilder sb = new StringBuilder()
                        .append("Свободные записи")
                        .append(":\n\n");
                recordsForMsg.forEach((key, value) -> sb.append(new BoldString(key)).append("\nДаты выездов:\n").append(new MarkupList<>(value.stream().filter(HospitalRecord::hasPlace).map(HospitalRecord::getDateTimeForButton).toList())).append("\n"));
                sb.append("\n").append(new BoldString("Выберите ОПК"));
                return sb.toString();
            }

            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder) {
                ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder builder1 = Utils.getReplyKeyboardMarkupBuilder(recordsForMsg.keySet().stream().map(KeyboardButton::new).toList());
                builder.replyMarkup(builder1.build());
            }

            @Override
            public void action(long userId, String userText) {
                filteredRecords = recordsForMsg.get(userText).stream().filter(HospitalRecord::hasPlace).toList();
            }

        },
        STEP1 {

            @Override
            public String getMessageAnswer(String userText) {
                return "Записи в ОПК " + userText +
                        ":\n" +
                        "\nДаты выездов:\n" +
                        new MarkupList<>(filteredRecords.stream().map(HospitalRecord::getDateTimeForButton).toList()) + "\n" + new BoldString("Выберите время");
            }

            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder) {
                ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder builder1 = Utils.getReplyKeyboardMarkupBuilder(filteredRecords.stream().map(HospitalRecord::getDateTimeForButton).map(KeyboardButton::new).toList());
                builder.replyMarkup(builder1.build());
            }

            @Override
            public void action(long userId, String userText) {
                answer = filteredRecords.stream().filter(r -> r.getDateTimeForButton().equals(userText)).findAny().orElse(null);
                answer.addUser(userId);
                scheduleService.saveRecord(answer);
            }
        },
        STEP3 {

            @Override
            public String getMessageAnswer(String userText) {
                return answer.getUsers().toString();
            }

            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder) {
                builder.replyMarkup(new ReplyKeyboardRemove(true));
            }
        };

        private static HospitalRecord answer;

        private static List<HospitalRecord> records;

        private static List<HospitalRecord> filteredRecords;

        private static Map<String, List<HospitalRecord>> recordsForMsg;
        private static ScheduleService scheduleService;

        private static void init() {
            records = scheduleService.getAllRecords();
        }

    }

    public SignupUser(ScheduleService scheduleService) {
        SignupSteps.scheduleService = scheduleService;
        SignupSteps.init();
    }

}
