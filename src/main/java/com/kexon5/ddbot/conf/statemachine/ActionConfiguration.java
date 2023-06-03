package com.kexon5.ddbot.conf.statemachine;

import com.kexon5.ddbot.bot.services.ActionElement;
import com.kexon5.ddbot.bot.services.administration.actions.GrantRoles;
import com.kexon5.ddbot.bot.services.edithospital.actions.AddHospital;
import com.kexon5.ddbot.bot.services.edithospital.actions.EditHospital;
import com.kexon5.ddbot.bot.services.hospital.actions.OpenRegistration;
import com.kexon5.ddbot.bot.services.mainmenu.actions.CheckInUser;
import com.kexon5.ddbot.bot.services.mainmenu.actions.CheckOutUser;
import com.kexon5.ddbot.bot.services.mainmenu.actions.SignUpUser;
import com.kexon5.ddbot.bot.services.schedule.actions.CreateSchedule;
import com.kexon5.ddbot.bot.services.schedule.actions.ReadSchedule;
import com.kexon5.ddbot.repositories.HospitalBackupRepository;
import com.kexon5.ddbot.repositories.HospitalRepository;
import com.kexon5.ddbot.repositories.UserRepository;
import com.kexon5.ddbot.services.RepositoryService;
import com.kexon5.ddbot.statemachine.DialogueFlow;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.telegram.abilitybots.api.objects.ReplyCollection;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@DependsOn("dbContext")
public class ActionConfiguration {
    @Bean
    public SignUpUser signUpUser(UserRepository userRepository) {
        return new SignUpUser(userRepository);
    }

    @Bean
    public CheckInUser checkInUser(RepositoryService repositoryService) {
        return new CheckInUser(repositoryService);
    }

    @Bean
    public CheckOutUser checkOutUser(RepositoryService repositoryService) {
        return new CheckOutUser(repositoryService);
    }

    @Bean
    public AddHospital addHospital(HospitalRepository hospitalRepository) {
        return new AddHospital(hospitalRepository);
    }

    @Bean
    public CreateSchedule createSchedule(RepositoryService repositoryService) {
        return new CreateSchedule(repositoryService);
    }

    @Bean
    public ReadSchedule readSchedule(RepositoryService repositoryService) {
        return new ReadSchedule(repositoryService);
    }

    @Bean
    public EditHospital editHospital(HospitalRepository hospitalRepository, HospitalBackupRepository hospitalBackupRepository) {
        return new EditHospital(hospitalRepository, hospitalBackupRepository);
    }

    @Bean
    public OpenRegistration openRegistration(RepositoryService repositoryService) {
        return new OpenRegistration(repositoryService);
    }

    @Bean
    public GrantRoles grantRoles(UserRepository userRepository) {
        return new GrantRoles(userRepository);
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
