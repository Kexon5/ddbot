package com.kexon5.ddbot.actions.hospitals;

import com.kexon5.ddbot.models.hospital.HospitalRecord;
import com.kexon5.ddbot.services.ScheduleService;
import com.kexon5.ddbot.statemachine.BotState;
import com.kexon5.ddbot.statemachine.Eventable;
import com.kexon5.ddbot.utils.markup.BoldString;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ReadSchedule {

    public enum ReadSteps implements BotState, Eventable {
        READ() {

            @Override
            public String getMessageAnswer(String userText) {
                StringBuilder sb = new StringBuilder().append("Считаны следующие записи:\n");
                LocalDate lastDate = null;
                for (HospitalRecord record : records) {
                    if (!Objects.equals(lastDate, record.getLocalDate())) {
                        lastDate = record.getLocalDate();
                        sb.append("\n")
                                .append(new BoldString(lastDate.format(DateTimeFormatter.ofPattern("E, dd.MM"))))
                                .append("\n\n");
                    }
                    sb.append(record.toAdminString()).append("\n");
                }
                return sb.toString();
            }

            @Override
            public boolean validate(String text) {
                try {
                    data = scheduleService.readTable();
                    date = LocalDate.parse(data.get(2).get(2).toString(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));

                    List<Object> schema = data.get(1);
                    schemaForTwoWeeksSize = schema.size();
                    schemaSize = schemaForTwoWeeksSize / 2;
                    dayOfWeekRowSize = schemaSize + 2;

                    return HospitalRecord.checkSchema(schema.subList(0, schemaSize)); //? null : "Схема не сходится с эталоном\nПересоздайте таблицу!";
                } catch (Exception e) {
//          todo Ну, тут нужна более правильная обработка
                    return false;//"Какая-то беда...";
                }
            }

            @Override
            public void action(long userId, String userText) {
                records.clear();

                int activeDayOfWeek = 0;
                for (int i = 3; i < data.size(); i++) {
                    List<Object> activeData = data.get(i);
                    if (activeData.size() == dayOfWeekRowSize && activeData.get(1).toString().isEmpty()) {
                        activeDayOfWeek++;
                    } else if (activeData.size() == schemaForTwoWeeksSize || activeData.size() == schemaSize) {
                        addRecord(activeData.subList(0, schemaSize), date.plusDays(activeDayOfWeek));
                        if (activeData.size() == schemaForTwoWeeksSize)
                            addRecord(activeData.subList(dayOfWeekRowSize - 1, schemaForTwoWeeksSize), date.plusDays(activeDayOfWeek + 7));
                    }
                }

                Collections.sort(records);
                scheduleService.saveRecords(records);
            }

            private void addRecord(List<Object> recordFields, LocalDate dateToUse) {
                if (recordFields.stream().anyMatch(field -> ((String) field).isBlank())) {
                    return;
                }
                records.add(new HospitalRecord(recordFields, dateToUse));
            }
        };

        private static ScheduleService scheduleService;
        private static List<List<Object>> data;
        private static LocalDate date;

        private static int schemaForTwoWeeksSize;
        private static int schemaSize;
        private static int dayOfWeekRowSize;

        private static List<HospitalRecord> records = new ArrayList<>();
    }

    public ReadSchedule(ScheduleService scheduleService) {
        ReadSteps.scheduleService = scheduleService;
    }

}
