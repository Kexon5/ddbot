package com.kexon5.bot.services;

import com.google.api.services.sheets.v4.model.ValueRange;
import com.kexon5.bot.models.ElementSetting;
import com.kexon5.bot.models.TableOption;
import com.kexon5.bot.models.google.GoogleSetting;
import com.kexon5.bot.models.hospital.Hospital;
import com.kexon5.bot.models.hospital.HospitalRecord;
import com.kexon5.bot.repositories.ElementSettingRepository;
import com.kexon5.bot.repositories.HospitalRecordRepository;
import com.kexon5.bot.repositories.HospitalRepository;
import com.kexon5.common.utils.ButtonUtils;
import com.kexon5.common.models.User;
import com.kexon5.common.repositories.UserRepository;
import com.kexon5.common.statemachine.Accessable;
import com.kexon5.common.statemachine.Element;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.kexon5.bot.models.hospital.Hospital.getSimpleStringList;
import static com.kexon5.bot.utils.DateUtils.getCurrentDirectory;

@RequiredArgsConstructor
public class RepositoryService {

    private final GoogleSettingsService googleSettingsService;
    private final HospitalRepository hospitalRepository;
    private final HospitalRecordRepository hospitalRecordRepository;

    private final UserRepository userRepository;
    private final ElementSettingRepository settingRepository;

    @Getter
    private GoogleSetting lastSchedule;


    public GoogleSetting getSchedule() {
        if (lastSchedule != null) return lastSchedule;

        String dirName = getCurrentDirectory();
        String fileName = "Расписание выездов " + dirName;

        lastSchedule = Optional.ofNullable(googleSettingsService.getGoogleSetting(fileName)).orElseGet(() -> {
            GoogleSetting fileSetting = googleSettingsService.getFileSettingViaTemplate("SCHEDULE", dirName, fileName);
            fillSchedule(fileSetting);
            return fileSetting;
        });

        return lastSchedule;
    }

    public ReplyKeyboardMarkup getTablesMarkup() {
        return ButtonUtils.getReplyKeyboardMarkupBuilder(TableOption.valueList).build();
    }

    public TableOption isTableOption(String userText) {
        return Arrays.stream(TableOption.values())
                     .filter(t -> t.getText().equals(userText))
                     .findFirst()
                     .orElse(null);
    }

    public boolean existOpenRecords() {
        return hospitalRecordRepository.findAllByStateEquals(HospitalRecord.RecordState.OPEN).stream()
                                       .anyMatch(HospitalRecord::hasPlace);
    }

    public List<HospitalRecord> findAllHospitalRecordsByHash(Set<Integer> hashes) {
        return hospitalRecordRepository.findHospitalRecordsByRecordHashIn(hashes);
    }

    public void deleteAllRecords(List<HospitalRecord> records) {
        hospitalRecordRepository.deleteAll(records);
    }


    public void userAction(long userId, Consumer<User> consumer) {
        User user = userRepository.findByUserId(userId);
        consumer.accept(user);
        userRepository.save(user);
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
        googleSettingsService.updateSheet(
                fileSetting.getGoogleId(),
                "Settings",
                new ValueRange()
                        .setValues(getHospitalsDataForSchedule())
                        .setMajorDimension("COLUMNS")
        );
    }

    public Map<TableOption, List<HospitalRecord>> readTable(TableOption option) {
        Set<TableOption> loadingSet = option.getTableOptions();

        return googleSettingsService.readTable(getSchedule().getGoogleId(), loadingSet);
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

    public List<HospitalRecord> getAllRecords(HospitalRecord.RecordState state) {
        return hospitalRecordRepository.findAllByStateEquals(state);
    }

    public User getUserByUserId(long userId) {
        return userRepository.findByUserId(userId);
    }

    public boolean userHasActiveRecord(long userId) {
        return getUserActiveRecord(userId) != null;
    }

    public HospitalRecord getUserActiveRecord(long userId) {
        return Optional.ofNullable(getUserByUserId(userId).getRecords())
                .map(records -> records.isEmpty()
                        ? null
                        : records.get(records.size() - 1))
                .filter(recordPair -> recordPair.getMiddle().equals(User.UserRecordStatus.REGISTERED))
                       .map(recordPair -> hospitalRecordRepository.findById(recordPair.getLeft()).get())
                       .orElse(null);
    }

    public <T extends Element<? extends Accessable>> T fillAccessPredicate(T serviceOrAction, ElementSetting.Type type) {
        if (!settingRepository.existsByElementName(serviceOrAction.name())) {
            settingRepository.createNew(serviceOrAction.name(), type);
        }

        Predicate<Long> userAccessCheck = userId -> Optional.ofNullable(userRepository.findByUserId(userId))
                                                            .map(serviceOrAction::hasAccess)
                                                            .orElse(false);

        Predicate<Long> stateWorkingCheck = userId -> settingRepository.isWorking(serviceOrAction.name());

        serviceOrAction.setAccessPredicate(userAccessCheck.and(stateWorkingCheck));

        return serviceOrAction;
    }

}
