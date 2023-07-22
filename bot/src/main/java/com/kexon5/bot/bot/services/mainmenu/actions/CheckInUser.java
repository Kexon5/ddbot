package com.kexon5.bot.bot.services.mainmenu.actions;

import com.kexon5.bot.bot.elements.ActionElement;
import com.kexon5.bot.bot.states.ActionState;
import com.kexon5.bot.models.hospital.HospitalRecord;
import com.kexon5.bot.services.RepositoryService;
import com.kexon5.bot.utils.Utils;
import com.kexon5.bot.utils.markup.BoldString;
import com.kexon5.bot.utils.markup.MarkupList;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class CheckInUser extends ActionElement {

    public CheckInUser(ActionState actionState,
                       Predicate<Long> predicate,
                       RepositoryService repositoryService) {
        super(
                actionState,
                predicate,
                CheckInSteps.values()
        );

        CheckInSteps.repositoryService = repositoryService;
    }

    @RequiredArgsConstructor
    public enum CheckInSteps implements ActionMessageState {
        SELECT_HOSPITAL {
            @Override
            public void initAction(long userId, Document userDocument) {
                List<HospitalRecord> records = repositoryService.getAllRecords(HospitalRecord.RecordState.OPEN).stream()
                                                                .filter(HospitalRecord::hasPlace)
                                                                .toList();

                userDocument.append(RECORDS_DOC, new Document(records.stream()
                                                                       .collect(Collectors.groupingBy(HospitalRecord::getHospital, Collectors.toList()))));
            }

            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, Document document) {
                Document recordsForMsg = document.get(RECORDS_DOC, Document.class);

                builder.replyMarkup(Utils.getReplyKeyboardMarkupBuilder(recordsForMsg.keySet()).build());
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document document) {
                StringBuilder sb = new StringBuilder()
                        .append("Свободные записи")
                        .append(":\n\n");

                document.get(RECORDS_DOC, Document.class).forEach((key, value) -> sb.append(new BoldString(key))
                                                        .append("\nДаты выездов:\n")
                                                        .append(new MarkupList<>(((List<HospitalRecord>)value).stream()
                                                                                      .map(HospitalRecord::getDateTimeForButton)
                                                                                      .toList()))
                                                        .append("\n"));

                return sb.append("\n").append(new BoldString("Выберите ОПК"))
                         .toString();
            }

            @Override
            public List<HospitalRecord> validate(long userId, String userText, Document document) {
                Document recordsForMsg = document.get(RECORDS_DOC, Document.class);

                return recordsForMsg.containsKey(userText)
                        ? recordsForMsg.getList(userText, HospitalRecord.class)
                        : null;
            }

        },
        SELECT_TIME {
            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, Document document) {
                ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder builder1 = Utils.getReplyKeyboardMarkupBuilder(document.getList(
                                                                                                                              SELECT_HOSPITAL.name(), HospitalRecord.class).stream()
                                                                                                                      .map(HospitalRecord::getDateTimeForButton)
                                                                                                                      .toList());
                builder.replyMarkup(builder1.build());
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document document) {
                return "Записи в ОПК " + userText + ":\n\nДаты выездов:\n" +
                        new MarkupList<>(document.getList(SELECT_HOSPITAL.name(), HospitalRecord.class).stream()
                                                 .map(HospitalRecord::getDateTimeForButton)
                                                 .toList()) +
                        "\n" + new BoldString("Выберите время");
            }

            @Override
            public HospitalRecord validate(long userId, String userText, Document document) {
                return document.getList(SELECT_HOSPITAL.name(), HospitalRecord.class).stream()
                               .filter(r -> r.getDateTimeForButton().equals(userText))
                               .findAny()
                               .orElse(null);
            }
        },
        CHECK_IN_RESULT {
            @Override
            public void finalAction(long userId, @Nullable String userText, Document document) {
                HospitalRecord answer = document.get(SELECT_TIME.name(), HospitalRecord.class);

                // todo add valid step
                repositoryService.checkInUser(answer, userId);
            }

            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, Document document) {
                builder.replyMarkup(new ReplyKeyboardRemove(true));
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document document) {
                return document.get(SELECT_TIME.name(), HospitalRecord.class).getUsers().toString();
            }

        };

        private static final String RECORDS_DOC = "RECORDS_DOC";

        private static RepositoryService repositoryService;

    }

}
