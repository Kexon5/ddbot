package com.kexon5.ddbot.bot.services.hospital.actions;

import com.kexon5.ddbot.bot.services.ActionElement;
import com.kexon5.ddbot.models.hospital.HospitalRecord;
import com.kexon5.ddbot.services.ScheduleService;
import com.kexon5.ddbot.utils.markup.BoldString;
import com.kexon5.ddbot.utils.markup.MarkupList;
import lombok.Getter;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.kexon5.ddbot.bot.services.ActionState.READ_SCHEDULE;
import static com.kexon5.ddbot.utils.Utils.*;

public class ReadSchedule extends ActionElement {

    public ReadSchedule(ScheduleService scheduleService) {
        super(READ_SCHEDULE, ReadSteps.values());

        ReadSteps.scheduleService = scheduleService;
    }

    public enum ReadSteps implements ActionMessageState {
        READ() {
            @Override
            public void initAction(long userId, Document userDocument) {
                TableData tableData = new TableData(scheduleService.readTable());

                if (tableData.checkSchema()) {
                    userDocument.append("RECORDS", tableData.createRecords());
                }
            }

            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, Document document) {
                builder.replyMarkup(YES_NO.build());
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument)  {
                if (!userDocument.containsKey("RECORDS")) return "Ошибка в схеме таблицы";

                StringBuilder sb = new StringBuilder().append("Считаны следующие записи:\n");
                List<HospitalRecord> records = userDocument.getList("RECORDS", HospitalRecord.class);

                fillStringBuilder(sb, records);

                LocalDateTime left = records.get(0).getDate().withHour(0);
                LocalDateTime right = records.get(records.size() - 1).getDate().withHour(23);
                records = scheduleService.findAllRecords(left, right);
                userDocument.append("LEFT", left)
                            .append("RIGHT", right);
                if (!records.isEmpty()) {
                    sb.append("\nПри добавлении сотрутся данные об этих записях:\n");

                    fillStringBuilder(sb, records);

                    MarkupList<String> users = records.stream()
                                                      .map(HospitalRecord::getUsers)
                                                      .flatMap(t -> scheduleService.findAllById(t).stream())
                                                      .map(user -> user.getName() + ", " + user.getPhoneNumber())
                                                      .collect(Collectors.toCollection(MarkupList::new));

                    if (!users.isEmpty()) {
                        sb.append("\nТак же будет необходимо перезаписать следующих доноров: \n\n")
                          .append(users);
                    }
                }
                return sb.append(new BoldString("\nВы уверены, что хотите продолжить?"))
                         .toString();
            }

            @Override
            public String validate(long userId, String userText, Document document) {
                return userText.equals(YES) || userText.equals(NO)
                        ? userText
                        : null;
            }

            @Override
            public String errorText() {
                return "Да или нет...";
            }

        },
        ACCEPT {
            @Override
            public void finalAction(long userId, @Nullable String userText, Document document) {
                if (document.getString(READ.name()).equals(YES)) {
                    LocalDateTime left = document.get("LEFT", LocalDateTime.class);
                    LocalDateTime right = document.get("RIGHT", LocalDateTime.class);
                    scheduleService.deleteAllByDateBetween(left, right);
                    scheduleService.saveRecords(document.getList("RECORDS", HospitalRecord.class));
                }
            }

            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, Document document) {
                builder.replyMarkup(new ReplyKeyboardRemove(true));
            }

            @Override
            public String getAnswer(@Nullable String userText, @NotNull Document document) {
                return document.getString(READ.name()).equals(YES)
                        ? "Успешно выполнено"
                        : "Успешно ничего не сделано";
            }
        };

        @Getter
        private static class TableData {

            private static final List<Object> savedSchema = List.of("Время", "Место сбора", "Место", "Тип выезда", "Кол-во человек");

            private final List<List<Object>> data;
            private final List<Object> currentSchema;
            private final LocalDate date;

            private final int schemaForTwoWeeksSize;
            private final int schemaSize;
            private final int dayOfWeekRowSize;

            public TableData(List<List<Object>> data) {
                this.data = data;
                this.date = LocalDate.parse(data.get(2).get(2).toString(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));

                List<Object> schema = data.get(1);
                this.schemaForTwoWeeksSize = schema.size();
                this.schemaSize = this.schemaForTwoWeeksSize / 2;
                this.dayOfWeekRowSize = this.schemaSize + 2;

                this.currentSchema = schema.subList(0, schemaSize);
            }

            public boolean checkSchema() {
                if (currentSchema.size() != savedSchema.size()) return false;
                return Objects.equals(currentSchema, savedSchema);
            }

            private void addRecord(List<HospitalRecord> records,
                                   List<Object> recordFields,
                                   LocalDate dateToUse) {
                if (recordFields.stream().anyMatch(field -> ((String) field).isBlank())) {
                    return;
                }
                records.add(new HospitalRecord(recordFields, dateToUse));
            }

            public List<HospitalRecord> createRecords() {
                List<HospitalRecord> records = new ArrayList<>();

                int activeDayOfWeek = 0;
                for (int i = 3; i < data.size(); i++) {
                    List<Object> activeData = data.get(i);
                    if (activeData.size() == dayOfWeekRowSize && activeData.get(1).toString().isEmpty()) {
                        activeDayOfWeek++;
                    } else if (activeData.size() == schemaForTwoWeeksSize || activeData.size() == schemaSize) {
                        addRecord(
                                records,
                                activeData.subList(0, schemaSize),
                                date.plusDays(activeDayOfWeek)
                        );
                        if (activeData.size() == schemaForTwoWeeksSize)
                            addRecord(
                                    records,
                                    activeData.subList(dayOfWeekRowSize - 1, schemaForTwoWeeksSize),
                                    date.plusDays(activeDayOfWeek + 7)
                            );
                    }
                }

                Collections.sort(records);
                return records;
            }
        }

        private static ScheduleService scheduleService;

    }
}
