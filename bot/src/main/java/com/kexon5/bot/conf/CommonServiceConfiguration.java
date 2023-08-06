package com.kexon5.bot.conf;

import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;
import com.kexon5.bot.bot.BotWebsocketClient;
import com.kexon5.bot.bot.DDBot;
import com.kexon5.bot.repositories.ElementSettingRepository;
import com.kexon5.bot.repositories.GoogleSettingRepository;
import com.kexon5.bot.repositories.HospitalRecordRepository;
import com.kexon5.bot.repositories.HospitalRepository;
import com.kexon5.bot.services.*;
import com.kexon5.common.models.ActiveEnvironment;
import com.kexon5.common.repositories.MailingGroupRepository;
import com.kexon5.common.repositories.UserRepository;
import com.kexon5.common.services.MailingService;
import com.kexon5.common.statemachine.Element;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.db.MapDBContext;
import org.telegram.abilitybots.api.sender.SilentSender;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
                                               UserRepository userRepository,
                                               ElementSettingRepository settingRepository) {
        return new RepositoryService(
                googleSettingsService,
                hospitalRepository,
                hospitalRecordRepository,
                userRepository,
                settingRepository
        );
    }

    @Bean(initMethod = "init")
    public BotWebsocketClient websocketClient(DDBot bot,
                                              ActiveEnvironment activeEnvironment,
                                              MethodUnicaster methodUnicaster,
                                              @Value("${publisher.address}") String url,
                                              @Value("${threads.count}") int threads) {
        ExecutorService exe = Executors.newFixedThreadPool(threads);
        return new BotWebsocketClient(bot, activeEnvironment, methodUnicaster, url, exe);
    }

    @Bean
    public MailingService taskManager(MailingGroupRepository mailingGroupRepository,
                                      ThreadPoolTaskScheduler threadPoolTaskScheduler) {
        return new MailingService(mailingGroupRepository, threadPoolTaskScheduler);
    }

    @Bean
    public DBContext dbContext() {
        DBContext test = MapDBContext.offlineInstance("TEST");
        Element.setDbContext(test);
        return test;
    }

    @Bean
    public NotificationRecordService notificationRecordService(HospitalRecordRepository hospitalRecordRepository,
                                                               UserRepository userRepository,
                                                               SilentSender sender,
                                                               List<NotificationSubscriber> subscribers) {
        var notificationRecordService = new NotificationRecordService(hospitalRecordRepository, userRepository, sender);
        notificationRecordService.addSubscribers(subscribers);
        return notificationRecordService;
    }

    @Bean
    public DepartureTableMakerService createTableRecordsService(GoogleSettingsService googleSettingsService,
                                                                UserRepository userRepository,
                                                                MailingService mailingService) {
        return new DepartureTableMakerService(googleSettingsService, userRepository, mailingService);
    }

//    @Bean
//    public Map<Long, Document> dbActionContext(BackupContextRepository backupContextRepository,
//                                               ThreadPoolTaskScheduler threadPoolTaskScheduler) {
//
//        // TODO: 04.06.2023 add this task to handler service with interactive menu
////        Duration defaultWindow = Duration.of(30L, ChronoUnit.SECONDS);
////        Duration deleteAfter = defaultWindow.multipliedBy(5);
////
////        threadPoolTaskScheduler.scheduleAtFixedRate(() -> {
////            BackupContext backupContext = new BackupContext(dbContext.backup(), dbActionContext);
////            backupContextRepository.save(backupContext);
////            backupContextRepository.deleteAllByDateTimeBefore(LocalDateTime.now().minus(deleteAfter));
////        }, defaultWindow);
//
//        return dbActionContext;
//    }
}
