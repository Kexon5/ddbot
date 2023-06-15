package com.kexon5.ddbot.services;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.Permission;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.kexon5.ddbot.models.TableOption;
import com.kexon5.ddbot.models.google.GoogleSetting;
import com.kexon5.ddbot.models.hospital.HospitalRecord;
import com.kexon5.ddbot.repositories.GoogleSettingRepository;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class GoogleSettingsService {
    private static final String ROOT_DIRECTORY = "BOT_DIRECTORY";

    private final GoogleSettingRepository googleSettingRepository;
    private final Drive drive;
    private final Sheets sheets;

    private String rootDirectory;

    public GoogleSettingsService(Drive drive,
                                 Sheets sheets,
                                 GoogleSettingRepository googleSettingRepository) {
        this.drive = drive;
        this.sheets = sheets;
        this.googleSettingRepository = googleSettingRepository;
    }

    public void init() {
        this.rootDirectory = getGoogleId(ROOT_DIRECTORY);
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
        String dirId = checkOrCreateDirectory(dirName);
        return createFileFromTemplate(templateName, fileName, dirId);
    }

    public String checkOrCreateDirectory(String dirName) {
        return Optional.ofNullable(getGoogleId(dirName))
                .orElseGet(() -> this.createDirectory(dirName, rootDirectory));
    }

    public String createDirectory(String directoryName, String parentFolder) {
        File fileMetadata = new File()
                .setName(directoryName)
                .setParents(List.of(parentFolder))
                .setMimeType("application/vnd.google-apps.folder");

        File result = null;
        try {
            result = drive.files()
                    .create(fileMetadata)
                    .setFields("id, webViewLink")
                    .execute();
        } catch (Exception e) {
            log.error("Folder wasn't created: {}", e.getMessage());
        }
        updateGoogleSettingsCollection(directoryName, result);
        return result.getId();
    }

    public GoogleSetting createFileFromTemplate(String templateName, String fileName, String directoryId) {
        String templateId = getGoogleId(templateName);
        if (templateId == null) return null;

        File copiedFile = new File()
                .setParents(List.of(directoryId))
                .setName(fileName);

        try {
            File result = drive.files()
                    .copy(templateId, copiedFile)
                    .setFields("id, webViewLink")
                    .execute();

            return updateGoogleSettingsCollection(fileName, result);
        } catch (IOException e) {
            log.error("File wasn't created: {}", e.getMessage());
            return null;
        }
    }

    public GoogleSetting updateGoogleSettingsCollection(String elementName, File file) {
        return file != null && file.getId() != null && file.getWebViewLink() != null
                ? updateGoogleSettingsCollection(elementName, file.getId(), file.getWebViewLink())
                : null;
    }

    public GoogleSetting updateGoogleSettingsCollection(String name, String id, String link) {
        GoogleSetting newSetting = new GoogleSetting(name, id, link);
        googleSettingRepository.save(newSetting);
        return newSetting;
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
        try {
            return sheets.spreadsheets()
                    .values()
                    .get(sheetId, range)
                    .execute();
        } catch (Exception e) {
            return null;
        }
    }

    public void updateSheet(String fileId, String range, ValueRange valueRange) throws IOException {
        sheets.spreadsheets()
                .values()
                .update(fileId, range, valueRange)
                .setValueInputOption("RAW")
                .execute();
    }

    public void createPermissions(String fileId, String role, String typePermission) throws IOException {
        drive.permissions().create(fileId, new Permission().setRole(role).setType(typePermission)).execute();
    }
}
