package com.kexon5.ddbot.conf;

import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;
import com.kexon5.ddbot.bot.DDBot;
import com.kexon5.ddbot.repositories.GoogleSettingRepository;
import com.kexon5.ddbot.repositories.HospitalRecordRepository;
import com.kexon5.ddbot.repositories.HospitalRepository;
import com.kexon5.ddbot.repositories.UserRepository;
import com.kexon5.ddbot.services.GoogleSettingsService;
import com.kexon5.ddbot.services.MailingService;
import com.kexon5.ddbot.services.RepositoryService;
import com.kexon5.ddbot.statemachine.MenuElement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
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

    @Bean
    public RepositoryService repositoryService(GoogleSettingsService googleSettingsService,
                                               HospitalRepository hospitalRepository,
                                               HospitalRecordRepository hospitalRecordRepository,
                                               UserRepository userRepository) {
        return new RepositoryService(
                googleSettingsService,
                hospitalRepository,
                hospitalRecordRepository,
                userRepository
        );
    }

    @Bean
    public MailingService taskManager(ThreadPoolTaskScheduler threadPoolTaskScheduler,
                                      DDBot bot) {
        return new MailingService(threadPoolTaskScheduler, bot);
    }

    @Bean
    public DBContext dbContext() {
        DBContext test = MapDBContext.offlineInstance("TEST");
        MenuElement.setDbContext(test);
        return test;
    }
}
