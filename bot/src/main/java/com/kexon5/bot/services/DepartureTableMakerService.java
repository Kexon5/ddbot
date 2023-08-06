package com.kexon5.bot.services;

import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.kexon5.bot.models.google.GoogleSetting;
import com.kexon5.bot.models.hospital.HospitalRecord;
import com.kexon5.bot.utils.DateUtils;
import com.kexon5.bot.utils.markup.MarkupList;
import com.kexon5.common.models.User;
import com.kexon5.common.repositories.UserRepository;
import com.kexon5.common.services.MailingService;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.kexon5.common.services.MailingService.NEW_TABLES;
import static com.kexon5.common.utils.StringUtils.escape;

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
    private final MailingService mailingService;


    private String getMsgText(List<GoogleSetting> googleSettings) {
        List<String> tables = googleSettings.stream()
                                            .map(setting -> setting.getName() + " - " + escape(setting.getLink()))
                                            .toList();


        return "Привет!\n\nЯ тут сгенерировал списки выездов\n\n" +
                new MarkupList<>(tables);
    }

    @Override
    public void subscribe(List<HospitalRecord> records, LocalDate recordsDate) {
        List<GoogleSetting> googleSettings = new ArrayList<>();
        records.stream()
               .collect(Collectors.groupingBy(HospitalRecord::getHospital, Collectors.toList()))
               .forEach((hospital, recordList) -> Optional.ofNullable(createTable(hospital, recordsDate, recordList)).ifPresent(googleSettings::add));

        if (!googleSettings.isEmpty()) {
            mailingService.sendMsgs(NEW_TABLES, getMsgText(googleSettings));
        }
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

    public GoogleSetting createTable(String hospital, LocalDate recordsDate, List<HospitalRecord> records) {
        records.sort(HospitalRecord::compareTo);
        List<List<Object>> data = new ArrayList<>();

        records.forEach(record -> {
            var users = userRepository.findAllById(record.getUsers());
            if (users.isEmpty()) return;

            users.sort(Comparator.comparing(User::getName));
            String time = record.getDate().toLocalTime().toString();
            for (var user : users) {
                data.add(List.of(time, STATUSES.get(0), user.getHyperlink(), user.getPhoneNumber()));
                time = "";
            }

            data.add(Collections.emptyList());
            data.add(Collections.emptyList());
        });

        if (data.isEmpty()) return null;

        String fileName = String.format("%s (%s)", hospital, recordsDate.toString());
        String parentDirId = googleService.getGoogleId(DateUtils.getCurrentDirectory());
        GoogleSetting setting = googleService.getFileSettingViaTemplate(TEMPLATE_NAME, DIR_NAME, fileName, parentDirId, false);
        googleService.batchUpdate(setting.getGoogleId(), getUpdateRequest(data, hospital));

        return setting;
    }

}
