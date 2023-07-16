package com.kexon5.bot.conf.statemachine;

import com.kexon5.bot.bot.elements.ActionElement;
import com.kexon5.bot.bot.services.administration.actions.GrantRoles;
import com.kexon5.bot.bot.services.edithospital.actions.AddHospital;
import com.kexon5.bot.bot.services.edithospital.actions.EditHospital;
import com.kexon5.bot.bot.services.hospital.actions.OpenRegistration;
import com.kexon5.bot.bot.services.mainmenu.actions.CheckInUser;
import com.kexon5.bot.bot.services.mainmenu.actions.CheckOutUser;
import com.kexon5.bot.bot.services.mainmenu.actions.SignUpUser;
import com.kexon5.bot.bot.services.schedule.actions.CreateSchedule;
import com.kexon5.bot.bot.services.schedule.actions.ReadSchedule;
import com.kexon5.bot.bot.states.ActionState;
import com.kexon5.bot.models.ElementSetting;
import com.kexon5.bot.repositories.ElementSettingRepository;
import com.kexon5.bot.repositories.HospitalBackupRepository;
import com.kexon5.bot.repositories.HospitalRepository;
import com.kexon5.bot.services.RepositoryService;
import com.kexon5.bot.statemachine.DialogueFlow;
import com.kexon5.common.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.telegram.abilitybots.api.objects.ReplyCollection;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.kexon5.bot.bot.states.ActionState.*;

@Configuration
@DependsOn("dbContext")
public class ActionConfiguration {

    @Autowired
    public RepositoryService repositoryService;
    @Autowired
    public ElementSettingRepository settingRepository;

    public Predicate<Long> getAccessPredicate(ActionState state) {
        if (!settingRepository.existsByElementName(state.name())) {
            settingRepository.createNew(state.name(), ElementSetting.Type.ACTION);
        }

        Predicate<Long> userAccessCheck =  userId -> Optional.ofNullable(repositoryService.getUserByUserId(userId))
                                                             .map(state.getAccessPredicate())
                                                             .orElse(false);

        Predicate<Long> stateWorkingCheck = userId -> settingRepository.isWorking(state.name(), ElementSetting.Type.ACTION);

        return userAccessCheck.and(stateWorkingCheck);
    }

    @Bean
    public SignUpUser signUpUser(UserRepository userRepository) {
        return new SignUpUser(SIGN_UP_USER, userRepository);
    }

    @Bean
    public CheckInUser checkInUser(RepositoryService repositoryService) {
        return new CheckInUser(
                CHECK_IN_USER,
                getAccessPredicate(CHECK_IN_USER).and(userId ->
                        !repositoryService.userHasActiveRecord(userId) && repositoryService.existOpenRecords()),
                repositoryService
        );
    }

    @Bean
    public CheckOutUser checkOutUser(RepositoryService repositoryService) {
        return new CheckOutUser(
                CHECK_OUT_USER,
                getAccessPredicate(CHECK_OUT_USER).and(repositoryService::userHasActiveRecord),
                repositoryService
        );
    }

    @Bean
    public AddHospital addHospital(HospitalRepository hospitalRepository) {
        return new AddHospital(ADD_HOSPITAL, getAccessPredicate(ADD_HOSPITAL), hospitalRepository);
    }

    @Bean
    public CreateSchedule createSchedule(RepositoryService repositoryService) {
        return new CreateSchedule(CREATE_SCHEDULE, getAccessPredicate(CREATE_SCHEDULE), repositoryService);
    }

    @Bean
    public ReadSchedule readSchedule(RepositoryService repositoryService) {
        return new ReadSchedule(READ_SCHEDULE, getAccessPredicate(READ_SCHEDULE), repositoryService);
    }

    @Bean
    public EditHospital editHospital(HospitalRepository hospitalRepository, HospitalBackupRepository hospitalBackupRepository) {
        return new EditHospital(
                EDIT_HOSPITAL,
                getAccessPredicate(EDIT_HOSPITAL),
                hospitalRepository,
                hospitalBackupRepository
        );
    }

    @Bean
    public OpenRegistration openRegistration(RepositoryService repositoryService) {
        return new OpenRegistration(OPEN_REGISTRATION, getAccessPredicate(OPEN_REGISTRATION), repositoryService);
    }

    @Bean
    public GrantRoles grantRoles(UserRepository userRepository) {
        return new GrantRoles(GRANT_ROLES, getAccessPredicate(GRANT_ROLES), userRepository);
    }

    @Bean
    public ReplyCollection actionReplyCollection(List<ActionElement> actionList) {
        List<DialogueFlow> replies = actionList.stream()
                                            .map(ActionElement::getReplyFlowBuilder)
                                            .map(DialogueFlow.DialogueFlowBuilder::build)
                                            .collect(Collectors.toList());
        return new ReplyCollection(replies);
    }

}
