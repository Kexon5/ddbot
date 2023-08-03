package com.kexon5.bot.services;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.BatchUpdateValuesRequest;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.kexon5.bot.models.TableOption;
import com.kexon5.bot.models.google.GoogleSetting;
import com.kexon5.bot.models.hospital.HospitalRecord;
import com.kexon5.bot.repositories.GoogleSettingRepository;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Collectors;

import static com.kexon5.bot.utils.WrapUtils.wrapExceptionableCall;

@Slf4j
public class GoogleSettingsService {
    private static final String ROOT_DIRECTORY = "BOT_DIRECTORY";

    private final GoogleSettingRepository googleSettingRepository;
    private final Drive drive;
    private final Sheets sheets;

    private String rootDirectoryId;

    public GoogleSettingsService(Drive drive,
                                 Sheets sheets,
                                 GoogleSettingRepository googleSettingRepository) {
        this.drive = drive;
        this.sheets = sheets;
        this.googleSettingRepository = googleSettingRepository;
    }

    public void init() {
        this.rootDirectoryId = getGoogleId(ROOT_DIRECTORY);
    }

    public GoogleSetting getGoogleSetting(String fileOrDirectoryName) {
        return googleSettingRepository.findByName(fileOrDirectoryName);
    }

    public String getGoogleId(String fileOrDirectoryName) {
        return Optional.ofNullable(getGoogleSetting(fileOrDirectoryName))
                .map(GoogleSetting::getGoogleId)
                .orElse(null);
    }

    public String getGoogleLink(String fileOrDirectoryName) {
        return Optional.ofNullable(getGoogleSetting(fileOrDirectoryName))
                .map(GoogleSetting::getLink)
                .orElse(null);
    }

    public GoogleSetting getFileSettingViaTemplate(String templateName, String dirName, String fileName) {
        return getFileSettingViaTemplate(templateName, dirName, fileName, rootDirectoryId, true);
    }

    public GoogleSetting getFileSettingViaTemplate(String templateName, String dirName, String fileName, String parentDir, boolean needToSave) {
        String dirId = checkOrCreateDirectory(dirName, parentDir);
        return createFileFromTemplate(templateName, fileName, dirId, needToSave);
    }


    public String checkOrCreateDirectory(String dirName) {
        return checkOrCreateDirectory(dirName, rootDirectoryId);
    }

    public String checkOrCreateDirectory(String dirName, String parentDir) {
        return Optional.ofNullable(getGoogleId(dirName))
                       .orElseGet(() -> this.createDirectory(dirName, parentDir));
    }


    public String createDirectory(String directoryName, String parentFolder) {
        File fileMetadata = new File()
                .setName(directoryName)
                .setParents(List.of(parentFolder))
                .setMimeType("application/vnd.google-apps.folder");

        File result = wrapExceptionableCall(() -> drive.files()
                                                       .create(fileMetadata)
                                                       .setFields("id, webViewLink")
                                                       .execute(), "Folder wasn't created:");
        updateGoogleSettingsCollection(directoryName, result, true);
        return result.getId();
    }

    public GoogleSetting createFileFromTemplate(String templateName, String fileName, String directoryId, boolean needToSave) {
        String templateId = getGoogleId(templateName);
        if (templateId == null) return null;

        File copiedFile = new File()
                .setParents(List.of(directoryId))
                .setName(fileName);

        return wrapExceptionableCall(() -> {
            File result = drive.files()
                               .copy(templateId, copiedFile)
                               .setFields("id, webViewLink")
                               .execute();

            var googleSetting = updateGoogleSettingsCollection(fileName, result, needToSave);
            if (googleSetting != null) {
                createPermissions(googleSetting.getGoogleId(), "writer", "anyone");
            }

            return googleSetting;
        }, "File wasn't created:");
    }

    public GoogleSetting updateGoogleSettingsCollection(String elementName, File file, boolean needToSave) {
        if (file != null && file.getId() != null && file.getWebViewLink() != null) {
            GoogleSetting newSetting = new GoogleSetting(elementName, file.getId(), file.getWebViewLink());
            if (needToSave) {
                googleSettingRepository.save(newSetting);
            }
            return newSetting;
        }

        return null;
    }

    public Map<TableOption, List<HospitalRecord>> readTable(String sheetId, Set<TableOption> ranges) {
        return ranges.stream()
                     .collect(Collectors.toMap(r -> r,
                                               r -> Optional.ofNullable(readTable(sheetId, r.getNamedField()))
                                                            .map(r::getRecords)
                                                            .orElse(Collections.emptyList())
                                               // TODO: 10.06.2023 пронос ошибки
                     ));
    }

    @Nullable
    public ValueRange readTable(String sheetId, String range) {
        return wrapExceptionableCall(() -> sheets.spreadsheets()
                                          .values()
                                          .get(sheetId, range)
                                          .execute());
    }

    public void updateSheet(String fileId, String range, ValueRange valueRange) {
        wrapExceptionableCall(() -> sheets.spreadsheets()
                                          .values()
                                          .update(fileId, range, valueRange)
                                          .setValueInputOption("RAW")
                                          .execute());
    }

    public void createPermissions(String fileId, String role, String typePermission) {
        wrapExceptionableCall(() -> drive.permissions()
                                         .create(fileId, new Permission().setRole(role).setType(typePermission))
                                         .execute());
    }

    public void batchUpdate(String sheetId, BatchUpdateValuesRequest request) {
        wrapExceptionableCall(() -> sheets.spreadsheets()
                                          .values()
                                          .batchUpdate(sheetId, request)
                                          .execute());
    }

}
