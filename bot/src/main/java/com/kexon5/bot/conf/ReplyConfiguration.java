package com.kexon5.bot.conf;

import com.kexon5.bot.bot.elements.ActionElement;
import com.kexon5.bot.bot.services.account.AccountRecordService;
import com.kexon5.bot.bot.services.account.AccountService;
import com.kexon5.bot.bot.services.account.AccountSettingsSwitcher;
import com.kexon5.bot.bot.services.administration.ActionSwitcher;
import com.kexon5.bot.bot.services.administration.AdministrationService;
import com.kexon5.bot.bot.services.administration.ServiceSwitcher;
import com.kexon5.bot.bot.services.administration.actions.GrantRoles;
import com.kexon5.bot.bot.services.administration.actions.MailingByRole;
import com.kexon5.bot.bot.services.edithospital.EditHospitalService;
import com.kexon5.bot.bot.services.edithospital.actions.AddHospital;
import com.kexon5.bot.bot.services.edithospital.actions.EditHospital;
import com.kexon5.bot.bot.services.hospital.HospitalService;
import com.kexon5.bot.bot.services.hospital.actions.OpenRegistration;
import com.kexon5.bot.bot.services.mainmenu.MainMenuService;
import com.kexon5.bot.bot.services.mainmenu.actions.CheckInUser;
import com.kexon5.bot.bot.services.mainmenu.actions.CheckOutUser;
import com.kexon5.bot.bot.services.mainmenu.actions.SignUpUser;
import com.kexon5.bot.bot.services.schedule.ScheduleService;
import com.kexon5.bot.bot.services.schedule.actions.CreateSchedule;
import com.kexon5.bot.bot.services.schedule.actions.ReadSchedule;
import com.kexon5.bot.models.ElementSetting;
import com.kexon5.bot.repositories.ElementSettingRepository;
import com.kexon5.bot.repositories.HospitalBackupRepository;
import com.kexon5.bot.repositories.HospitalRecordRepository;
import com.kexon5.bot.repositories.HospitalRepository;
import com.kexon5.common.repositories.MailingGroupRepository;
import com.kexon5.common.services.MailingService;
import com.kexon5.bot.services.RepositoryService;
import com.kexon5.common.repositories.UserRepository;
import com.kexon5.common.statemachine.DialogueFlow;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.telegram.abilitybots.api.objects.ReplyCollection;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.kexon5.bot.bot.states.ActionState.*;
import static com.kexon5.bot.bot.states.ServiceState.*;

@Configuration
@DependsOn("dbContext")
public class ReplyConfiguration {

    @Bean
    public ReplyCollection menuReplyCollection(UserRepository userRepository,
                                               RepositoryService repositoryService,
                                               ElementSettingRepository settingRepository,
                                               HospitalRecordRepository hospitalRecordRepository,
                                               MailingGroupRepository mailingGroupRepository) {
        Stream.of(new HospitalService(HOSPITALS_MENU),
                  new EditHospitalService(EDIT_HOSPITALS_MENU, repositoryService),
                  new AdministrationService(ADMINISTRATION_MENU),
                  new ScheduleService(SCHEDULE_MENU),
                  new ServiceSwitcher(SERVICE_SWITCHER_MENU, settingRepository),
                  new ActionSwitcher(ACTION_SWITCHER_MENU, settingRepository),
                  new AccountSettingsSwitcher(ACCOUNT_SETTINGS_SWITCHER, userRepository, mailingGroupRepository),
                  new AccountRecordService(ACCOUNT_RECORD_MENU, userRepository, hospitalRecordRepository),
                  new AccountService(ACCOUNT_MENU)
              )
              .forEach(service -> repositoryService.fillAccessPredicate(service, ElementSetting.Type.SERVICE));


        return null;
    }

    @Bean
    public ReplyCollection actionReplyCollection(UserRepository userRepository,
                                                 RepositoryService repositoryService,
                                                 HospitalRepository hospitalRepository,
                                                 HospitalBackupRepository hospitalBackupRepository,
                                                 MailingService mailingService) {
        return new ReplyCollection(
                Stream.of(new SignUpUser(SIGN_UP_USER, userRepository),
                          new CheckInUser(CHECK_IN_USER, repositoryService),
                          new CheckOutUser(CHECK_OUT_USER, repositoryService),
                          new AddHospital(ADD_HOSPITAL, hospitalRepository),
                          new CreateSchedule(CREATE_SCHEDULE, repositoryService),
                          new ReadSchedule(READ_SCHEDULE, repositoryService),
                          new EditHospital(EDIT_HOSPITAL, hospitalRepository, hospitalBackupRepository),
                          new OpenRegistration(OPEN_REGISTRATION, repositoryService),
                          new GrantRoles(GRANT_ROLES, userRepository),
                          new MailingByRole(MAILING_BY_ROLE, userRepository, mailingService)
                      )
                      .map(action -> repositoryService.fillAccessPredicate(action, ElementSetting.Type.ACTION))
                      .map(ActionElement::getReplyFlowBuilder)
                      .map(DialogueFlow.DialogueFlowBuilder::build)
                      .collect(Collectors.toList())
        );
    }

    @Bean
    @DependsOn("menuReplyCollection")
    public MainMenuService mainMenu(UserRepository userRepository) {
        return new MainMenuService(MAIN_MENU, userRepository);
    }

}
