package com.kexon5.bot.services;

import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.kexon5.bot.models.google.GoogleSetting;
import com.kexon5.bot.models.hospital.HospitalRecord;
import com.kexon5.bot.utils.DateUtils;
import com.kexon5.common.models.User;
import com.kexon5.common.repositories.UserRepository;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class DepartureTableMakerService implements NotificationSubscriber {

    private static final String DIR_NAME = "Списки выездов";
    private static final String TEMPLATE_NAME = "RECORDS_TABLE";

    private static final List<Object> STATUSES = List.of("Непонятно где",
                                                         "Скоро будет",
                                                         "Вафлится где-то",
                                                         "На месте",
                                                         "Не сдал",
                                                         "Сдал"
    );

    private final GoogleSettingsService googleService;
    private final UserRepository userRepository;


    @Override
    public void subscribe(List<HospitalRecord> records, LocalDate recordsDate) {
        records.stream()
               .collect(Collectors.groupingBy(HospitalRecord::getHospital, Collectors.toList()))
               .forEach((hospital, recordList) -> createTable(hospital, recordsDate, recordList));
    }

    @Override
    public HospitalRecord.RecordState getSubscriberType() {
        return HospitalRecord.RecordState.OPEN;
    }

    private BatchUpdateValuesRequest getUpdateRequest(List<List<Object>> data, String hospital) {
        return new BatchUpdateValuesRequest()
                .setValueInputOption("USER_ENTERED")
                .setData(List.of(new ValueRange()
                                         .setValues(data)
                                         .setRange("Data"),
                                 new ValueRange()
                                         .setValues(List.of(List.of(hospital)))
                                         .setRange("Hospital"),
                                 new ValueRange()
                                         .setValues(List.of(STATUSES))
                                         .setRange("Status")
                                         .setMajorDimension("COLUMNS")
                ));
    }


    public void createTable(String hospital, LocalDate recordsDate, List<HospitalRecord> records) {
        records.sort(HospitalRecord::compareTo);
        List<List<Object>> data = new ArrayList<>();

        records.forEach(record -> {
            var users = userRepository.findAllById(record.getUsers());
            users.sort(Comparator.comparing(User::getName));
            String time = record.getDate().toLocalTime().toString();
            for (var user : users) {
                data.add(List.of(time, STATUSES.get(0), user.getHyperlink(), user.getPhoneNumber()));
                time = "";
            }

            data.add(Collections.emptyList());
            data.add(Collections.emptyList());
        });

        String fileName = String.format("%s (%s)", hospital, recordsDate.toString());
        String parentDirId = googleService.getGoogleId(DateUtils.getCurrentDirectory());
        GoogleSetting setting = googleService.getFileSettingViaTemplate(TEMPLATE_NAME, DIR_NAME, fileName, parentDirId, false);
        googleService.batchUpdate(setting.getGoogleId(), getUpdateRequest(data, hospital));
    }

}
