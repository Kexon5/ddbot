package com.kexon5.ddbot.services;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.kexon5.ddbot.models.User;
import com.kexon5.ddbot.models.google.GoogleSetting;
import com.kexon5.ddbot.models.hospital.Hospital;
import com.kexon5.ddbot.models.hospital.HospitalRecord;
import com.kexon5.ddbot.repositories.HospitalRecordRepository;
import com.kexon5.ddbot.repositories.HospitalRepository;
import com.kexon5.ddbot.repositories.UserRepository;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;

import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.kexon5.ddbot.models.hospital.Hospital.getSimpleStringList;

@RequiredArgsConstructor
public class RepositoryService {

    private static final int MONTH_FOR_SPRING_EVENT = 4;
    private static final int MONTH_FOR_FALL_EVENT = 10;

    private final GoogleSettingsService googleSettingsService;
    private final HospitalRepository hospitalRepository;
    private final HospitalRecordRepository hospitalRecordRepository;

    private final UserRepository userRepository;


    public Consumer<BaseAbilityBot> dailyTask() {
        return bot -> {
            List<HospitalRecord> records = hospitalRecordRepository.findAllByStateEquals(HospitalRecord.RecordState.CLOSED).stream()
                                                                   .filter(record -> record.getDate().isBefore(LocalDateTime.now()))
                                                                   .toList();

            List<User> usersList = records.stream()
                                          .peek(rec -> rec.setState(HospitalRecord.RecordState.OUTDATED))
                                          .map(HospitalRecord::getUsers)
                                          .flatMap(users -> userRepository.findAllById(users).stream())
                                          .peek(user -> user.setActiveRecord(null))
                                          .toList();

            userRepository.saveAll(usersList);
            hospitalRecordRepository.saveAll(records);
        };
    }

    @Getter
    private GoogleSetting lastSchedule;

    private static long getDaysBetween(LocalDate first, LocalDate second) {
        return Duration.between(first.atStartOfDay(), second.atStartOfDay()).toDays();
    }

    public GoogleSetting getSchedule() {
        if (lastSchedule != null) return lastSchedule;

        LocalDate date = LocalDate.now();

        long daysToSpringAct = getDaysBetween(date, LocalDate.of(date.getYear(), MONTH_FOR_SPRING_EVENT, 1));
        String dirName = (daysToSpringAct > 0 ? "Весна'" : "Осень'") + date.getYear() % 100;
        String fileName = "Расписание выездов " + dirName;

        lastSchedule = Optional.ofNullable(googleSettingsService.getGoogleSetting(fileName)).orElseGet(() -> {
            GoogleSetting fileSetting = googleSettingsService.getFileSettingViaTemplate("SCHEDULE", dirName, fileName);
            fillSchedule(fileSetting);
            return fileSetting;
        });

        return lastSchedule;
    }

    public boolean existOpenRecords() {
        return hospitalRecordRepository.existsHospitalRecordByStateEquals(HospitalRecord.RecordState.OPEN);
    }


    public void userAction(long userId, Consumer<User> consumer) {
        User user = userRepository.findByUserId(userId);
        consumer.accept(user);
        userRepository.save(user);
    }

    public void deleteAllByDateBetween(LocalDateTime date1, LocalDateTime date2) {
        hospitalRecordRepository.deleteAllByDateBetween(date1, date2);
    }

    public List<User> findAllById(Iterable<ObjectId> ids) {
        return userRepository.findAllById(ids);
    }

    public void checkOutUser(HospitalRecord record, long userId) {
        userAction(userId, user -> {
            record.removeUser(user);
            saveRecord(record);
        });
    }

    public void checkInUser(HospitalRecord record, long userId) {
        userAction(userId, user -> {
            record.addUser(user);
            saveRecord(record);
        });
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

    public List<HospitalRecord> findAllRecords(LocalDateTime date1, LocalDateTime date2) {
        return hospitalRecordRepository.findAllByDateBetween(date1, date2);
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

    public List<HospitalRecord> getAllRecords(HospitalRecord.RecordState state) {
        return hospitalRecordRepository.findAllByStateEquals(state);
    }

    public User getUserByUserId(long userId) {
        return userRepository.findByUserId(userId);
    }

    public boolean userHasActiveRecord(long userId) {
        return getUserByUserId(userId).getActiveRecord() != null;
    }

    public HospitalRecord getUserActiveRecord(long userId) {
        return Optional.ofNullable(getUserByUserId(userId).getActiveRecord())
                       .flatMap(hospitalRecordRepository::findById)
                       .orElse(null);
    }
}
