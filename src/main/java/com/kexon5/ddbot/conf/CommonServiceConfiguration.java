package com.kexon5.ddbot.conf;

import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;
import com.kexon5.ddbot.repositories.GoogleSettingRepository;
import com.kexon5.ddbot.repositories.HospitalRecordRepository;
import com.kexon5.ddbot.repositories.HospitalRepository;
import com.kexon5.ddbot.services.GoogleSettingsService;
import com.kexon5.ddbot.services.ScheduleService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CommonServiceConfiguration {
    @Bean(initMethod = "init")
    public GoogleSettingsService dataExchangeService(Drive drive,
                                                     Sheets sheets,
                                                     GoogleSettingRepository googleSettingRepository) {
        return new GoogleSettingsService(
                drive,
                sheets,
                googleSettingRepository);
    }

    @Bean
    public ScheduleService scheduleService(GoogleSettingsService googleSettingsService,
                                           HospitalRepository hospitalRepository,
                                           HospitalRecordRepository hospitalRecordRepository) {
        return new ScheduleService(googleSettingsService, hospitalRepository, hospitalRecordRepository);
    }
}
