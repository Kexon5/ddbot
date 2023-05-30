package com.kexon5.ddbot.conf;

import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;
import com.kexon5.ddbot.repositories.GoogleSettingRepository;
import com.kexon5.ddbot.repositories.HospitalRecordRepository;
import com.kexon5.ddbot.repositories.HospitalRepository;
import com.kexon5.ddbot.repositories.UserRepository;
import com.kexon5.ddbot.services.GoogleSettingsService;
import com.kexon5.ddbot.services.ScheduleService;
import com.kexon5.ddbot.statemachine.MenuElement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.db.MapDBContext;

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

    @Bean(initMethod = "init")
    public ScheduleService scheduleService(GoogleSettingsService googleSettingsService,
                                           HospitalRepository hospitalRepository,
                                           HospitalRecordRepository hospitalRecordRepository,
                                           UserRepository userRepository) {
        return new ScheduleService(
                googleSettingsService,
                hospitalRepository,
                hospitalRecordRepository,
                userRepository
        );
    }

    @Bean
    public DBContext dbContext() {
        DBContext test = MapDBContext.offlineInstance("TEST");
        MenuElement.setDbContext(test);
        return test;
    }
}
