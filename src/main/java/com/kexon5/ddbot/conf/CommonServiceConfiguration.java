package com.kexon5.ddbot.conf;

import com.google.api.services.drive.Drive;
import com.google.api.services.sheets.v4.Sheets;
import com.kexon5.ddbot.bot.DDBot;
import com.kexon5.ddbot.repositories.GoogleSettingRepository;
import com.kexon5.ddbot.repositories.HospitalRecordRepository;
import com.kexon5.ddbot.repositories.HospitalRepository;
import com.kexon5.ddbot.repositories.UserRepository;
import com.kexon5.ddbot.services.GoogleSettingsService;
import com.kexon5.ddbot.services.RepositoryService;
import com.kexon5.ddbot.services.TaskManager;
import com.kexon5.ddbot.statemachine.MenuElement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.db.MapDBContext;

import java.time.temporal.ChronoUnit;

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
    public RepositoryService repositoryService(TaskManager taskManager,
                                               GoogleSettingsService googleSettingsService,
                                               HospitalRepository hospitalRepository,
                                               HospitalRecordRepository hospitalRecordRepository,
                                               UserRepository userRepository) {
        RepositoryService repositoryService = new RepositoryService(
                googleSettingsService,
                hospitalRepository,
                hospitalRecordRepository,
                userRepository
        );

        taskManager.addRepeatableTask(repositoryService.dailyTask(), 1L, ChronoUnit.DAYS);

        return repositoryService;
    }

    @Bean(initMethod = "init")
    public TaskManager taskManager(DDBot bot) {
        return new TaskManager(bot);
    }

    @Bean
    public DBContext dbContext() {
        DBContext test = MapDBContext.offlineInstance("TEST");
        MenuElement.setDbContext(test);
        return test;
    }
}
