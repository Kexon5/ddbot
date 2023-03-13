package com.kexon5.ddbot.services;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.kexon5.ddbot.models.google.GoogleSetting;
import com.kexon5.ddbot.models.hospital.Hospital;
import com.kexon5.ddbot.models.hospital.HospitalRecord;
import com.kexon5.ddbot.repositories.HospitalRecordRepository;
import com.kexon5.ddbot.repositories.HospitalRepository;
import com.kexon5.ddbot.utils.Utils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.kexon5.ddbot.models.hospital.Hospital.getSimpleStringList;

@RequiredArgsConstructor
public class ScheduleService {

    private static final int MONTH_FOR_SPRING_EVENT = 4;
    private static final int MONTH_FOR_FALL_EVENT = 10;

    private final GoogleSettingsService googleSettingsService;
    private final HospitalRepository hospitalRepository;
    private final HospitalRecordRepository hospitalRecordRepository;

    @Getter
    private GoogleSetting lastSchedule;

    public GoogleSetting getSchedule() {
        if (lastSchedule != null) return lastSchedule;

        LocalDate date = LocalDate.now();

        long daysToSpringAct = Utils.getDaysBetween(date, LocalDate.of(date.getYear(), MONTH_FOR_SPRING_EVENT, 1));
        long daysToFallAct = Utils.getDaysBetween(date, LocalDate.of(date.getYear(), MONTH_FOR_FALL_EVENT, 1));
        String dirName = (daysToSpringAct < daysToFallAct ? "Весна'" : "Осень'") + date.getYear() % 100;
        String fileName = "Расписание выездов " + dirName;

        lastSchedule = Optional.ofNullable(googleSettingsService.getGoogleSetting(fileName)).orElseGet(() -> {
            GoogleSetting fileSetting = googleSettingsService.getFileSettingViaTemplate("SCHEDULE", dirName, fileName);
            fillSchedule(fileSetting);
            return fileSetting;
        });

        return lastSchedule;
    }

    public void fillSchedule(GoogleSetting fileSetting) {
        String fileId = fileSetting.getGoogleId();
        List<List<Object>> valuesList = getHospitalsDataForSchedule();

        ValueRange values = new ValueRange()
                .setValues(valuesList)
                .setMajorDimension("COLUMNS");

        try {
            googleSettingsService.updateSheet(fileId, "Settings", values);
            googleSettingsService.createPermissions(fileId, "writer", "anyone");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public List<List<Object>> readTable() {
        return Optional.ofNullable(googleSettingsService.readTable(getSchedule().getGoogleId(), "Hospitals"))
                .map(ValueRange::getValues)
                .orElse(null);
    }

    public List<List<Object>> getHospitalsDataForSchedule() {
        List<Hospital> hospitals = getAllHospitals();
        List<Object> hospitalNames = hospitals.stream().map(Hospital::getSimpleName).collect(Collectors.toList());
        List<Object> contacts = getSimpleStringList(hospitals, Hospital::getContactInfo);
        List<Object> requiredDocuments = getSimpleStringList(hospitals, Hospital::getRequiredDocuments);
        List<Object> notes = getSimpleStringList(hospitals, Hospital::getNotes);

        return List.of(hospitalNames, contacts, requiredDocuments, notes);
    }

    public void saveRecords(List<HospitalRecord> records) {
        hospitalRecordRepository.saveAll(records);
    }

    public void saveRecord(HospitalRecord record) {
        hospitalRecordRepository.save(record);
    }

    public List<Hospital> getAllHospitals() {
        return hospitalRepository.findAll();
    }

    public List<HospitalRecord> getAllRecords() {
        return hospitalRecordRepository.findAll();
    }

    public boolean userHasActiveRecord(long userId) {
        LocalDateTime date = LocalDateTime.of(2023, 2, 1, 0, 0);
        return hospitalRecordRepository.existsHospitalRecordByDateBetweenAndUsersContains(date, date.plusMonths(1), List.of(userId));
    }

    public HospitalRecord getUserActiveRecord(long userId) {
        LocalDateTime date = LocalDateTime.of(2023, 2, 1, 0, 0);
        return hospitalRecordRepository.findFirstByDateBetweenAndUsersContains(date, date.plusMonths(1), List.of(userId));
    }
}
