package com.kexon5.bot.models;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.kexon5.bot.models.hospital.HospitalRecord;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum TableOption {
    OPK("Отделения переливания", "Hospitals", List.of("Время", "Место сбора", "ОПК", "Тип выезда", "Кол-во человек")) {
        @Override
        public List<HospitalRecord> getRecords(List<List<Object>> data) {
            LocalDate actionStart = LocalDate.parse(data.get(2).get(2).toString(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));
            List<Object> schema = data.get(1);
            int schemaForTwoWeeksSize = schema.size();
            int schemaSize = schemaForTwoWeeksSize / 2;
            int dayOfWeekRowSize = schemaSize + 2;

            List<Object> currentSchema = schema.subList(0, schemaSize);
            if (!checkSchema(currentSchema, this.schema)) return null;

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
                            actionStart.plusDays(activeDayOfWeek)
                    );
                    if (activeData.size() == schemaForTwoWeeksSize)
                        addRecord(
                                records,
                                activeData.subList(dayOfWeekRowSize - 1, schemaForTwoWeeksSize),
                                actionStart.plusDays(activeDayOfWeek + 7)
                        );
                }
            }

            Collections.sort(records);
            return records;
        }

        private static void addRecord(List<HospitalRecord> records,
                                      List<Object> recordFields,
                                      LocalDate dateToUse) {
            if (recordFields.stream().anyMatch(field -> ((String) field).isBlank())) {
                return;
            }
            records.add(new HospitalRecord(recordFields, dateToUse));
        }

    },
    MONTHLY("Ежемесячные", "Monthly", List.of("Дата", "Время", "Место сбора", "ОПК", "Тип выезда", "Кол-во человек")) {
        @Override
        public List<HospitalRecord> getRecords(List<List<Object>> data) {
            List<Object> schema = data.get(1);
            int schemaSize = schema.size();

            List<Object> currentSchema = schema.subList(0, schemaSize);
            if (!checkSchema(currentSchema, this.schema)) return null;

            List<HospitalRecord> records = new ArrayList<>();

            for (int i = 2; i < data.size(); i++) {
                List<Object> activeData = data.get(i);
                if (activeData.size() == schemaSize) {
                    addRecord(records, activeData);
                }
            }

            Collections.sort(records);
            return records;
        }

        private static void addRecord(List<HospitalRecord> records,
                                      List<Object> recordFields) {
            if (recordFields.stream().anyMatch(field -> ((String) field).isBlank())) {
                return;
            }
            records.add(new HospitalRecord(recordFields));
        }
    },
    STUDCLUB("Студклуб", "Studclub", List.of("Время", "Кол-во человек")) {
        @Override
        public List<HospitalRecord> getRecords(List<List<Object>> data) {
            LocalDate actionStart = LocalDate.parse(data.get(0).get(5).toString(), DateTimeFormatter.ofPattern("dd.MM.yyyy"));

            List<Object> schema = data.get(2);
            int schemaSize = schema.size() / 5;

            List<Object> currentSchema = schema.subList(0, schemaSize);
            if (!checkSchema(currentSchema, this.schema)) return null;

            List<HospitalRecord> records = new ArrayList<>();

            for (int i = 3; i < data.size(); i++) {
                List<Object> activeData = data.get(i);
                addRecord(records, activeData, actionStart);
            }

            Collections.sort(records);
            return records;
        }

        private static void addRecord(List<HospitalRecord> records,
                                      List<Object> recordFields,
                                      LocalDate dateToUse) {
            for (int i = 0; i < recordFields.size(); i+= 2) {
                List<Object> schemaData = recordFields.subList(i, i + 2);
                if (schemaData.stream().noneMatch(field -> ((String) field).isBlank())) {
                    records.add(new HospitalRecord(schemaData, dateToUse.plusDays(i / 2)));
                }

            }
        }
    },
    ALL("Все", "ALL", List.of()) {

        @Nullable
        @Override
        public List<HospitalRecord> getRecords(List<List<Object>> data) {
            return null;
        }

    };


    final String text;
    final String namedField;
    final List<Object> schema;

    public static final List<String> valueList = Arrays.stream(values())
                                                       .map(t -> t.text)
                                                       .toList();

    public static final Set<TableOption> namedFieldSet = Arrays.stream(values())
                                                               .filter(option -> !option.equals(ALL))
                                                               .collect(Collectors.toSet());

    public Set<TableOption> getTableOptions() {
        return this.equals(ALL)
                ? namedFieldSet
                : Set.of(this);
    }

    @Nullable
    public List<HospitalRecord> getRecords(@Nullable ValueRange valueRange) {
        return getRecords(valueRange.getValues());
    }

    @Nullable
    public abstract List<HospitalRecord> getRecords(List<List<Object>> data);

    private static boolean checkSchema(List<Object> currentSchema, List<Object> schema) {
        if (currentSchema.size() != schema.size()) return false;
        return Objects.equals(currentSchema, schema);
    }

}
