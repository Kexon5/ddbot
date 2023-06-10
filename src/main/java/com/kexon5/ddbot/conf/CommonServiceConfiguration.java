package com.kexon5.ddbot.conf;

import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;
import com.kexon5.ddbot.bot.DDBot;
import com.kexon5.ddbot.bot.elements.ActionElement;
import com.kexon5.ddbot.repositories.*;
import com.kexon5.ddbot.services.GoogleSettingsService;
import com.kexon5.ddbot.services.MailingService;
import com.kexon5.ddbot.services.RepositoryService;
import com.kexon5.ddbot.statemachine.Element;
import org.bson.Document;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.db.MapDBContext;

import java.util.HashMap;
import java.util.Map;

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
        Element.setDbContext(test);
        return test;
    }

    @Bean
    public Map<Long, Document> dbActionContext(DBContext dbContext,
                                               BackupContextRepository backupContextRepository,
                                               ThreadPoolTaskScheduler threadPoolTaskScheduler) {
        Map<Long, Document> dbActionContext = new HashMap<>();
        ActionElement.setContextMap(dbActionContext);

        // TODO: 04.06.2023 add this task to handler service with interactive menu
//        Duration defaultWindow = Duration.of(30L, ChronoUnit.SECONDS);
//        Duration deleteAfter = defaultWindow.multipliedBy(5);
//
//        threadPoolTaskScheduler.scheduleAtFixedRate(() -> {
//            BackupContext backupContext = new BackupContext(dbContext.backup(), dbActionContext);
//            backupContextRepository.save(backupContext);
//            backupContextRepository.deleteAllByDateTimeBefore(LocalDateTime.now().minus(deleteAfter));
//        }, defaultWindow);

        return dbActionContext;
    }
}
