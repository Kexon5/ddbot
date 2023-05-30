package com.kexon5.ddbot.conf.statemachine;

import com.kexon5.ddbot.bot.services.ActionElement;
import com.kexon5.ddbot.bot.services.edithospital.actions.AddHospital;
import com.kexon5.ddbot.bot.services.edithospital.actions.EditHospital;
import com.kexon5.ddbot.bot.services.hospital.actions.CreateSchedule;
import com.kexon5.ddbot.bot.services.hospital.actions.OpenRegistration;
import com.kexon5.ddbot.bot.services.hospital.actions.ReadSchedule;
import com.kexon5.ddbot.bot.services.mainmenu.actions.CheckInUser;
import com.kexon5.ddbot.bot.services.mainmenu.actions.CheckOutUser;
import com.kexon5.ddbot.bot.services.mainmenu.actions.SignUpUser;
import com.kexon5.ddbot.repositories.HospitalBackupRepository;
import com.kexon5.ddbot.repositories.HospitalRepository;
import com.kexon5.ddbot.repositories.UserRepository;
import com.kexon5.ddbot.services.ScheduleService;
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
    public CheckInUser checkInUser(ScheduleService scheduleService) {
        return new CheckInUser(scheduleService);
    }

    @Bean
    public CheckOutUser checkOutUser(ScheduleService scheduleService) {
        return new CheckOutUser(scheduleService);
    }

    @Bean
    public AddHospital addHospital(HospitalRepository hospitalRepository) {
        return new AddHospital(hospitalRepository);
    }

    @Bean
    public CreateSchedule createSchedule(ScheduleService scheduleService) {
        return new CreateSchedule(scheduleService);
    }

    @Bean
    public ReadSchedule readSchedule(ScheduleService scheduleService) {
        return new ReadSchedule(scheduleService);
    }

    @Bean
    public EditHospital editHospital(HospitalRepository hospitalRepository, HospitalBackupRepository hospitalBackupRepository) {
        return new EditHospital(hospitalRepository, hospitalBackupRepository);
    }

    @Bean
    public OpenRegistration openRegistration(ScheduleService scheduleService) {
        return new OpenRegistration(scheduleService);
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
