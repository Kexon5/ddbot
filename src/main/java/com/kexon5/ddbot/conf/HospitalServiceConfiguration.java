package com.kexon5.ddbot.conf;

import com.kexon5.ddbot.actions.hospitals.CheckoutUser;
import com.kexon5.ddbot.actions.hospitals.CreateSchedule;
import com.kexon5.ddbot.actions.hospitals.ReadSchedule;
import com.kexon5.ddbot.actions.hospitals.SignupUser;
import com.kexon5.ddbot.actions.hospitals.edit.AddHospital;
import com.kexon5.ddbot.actions.hospitals.edit.EditHospital;
import com.kexon5.ddbot.repositories.HospitalBackupRepository;
import com.kexon5.ddbot.repositories.HospitalRepository;
import com.kexon5.ddbot.services.ScheduleService;
import com.kexon5.ddbot.statemachine.ActionStateHolder;
import com.kexon5.ddbot.statemachine.ServiceStateHolder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HospitalServiceConfiguration {

    @Bean
    public ServiceStateHolder serviceStateHolder(ScheduleService scheduleService) {
        return new ServiceStateHolder(scheduleService);
    }


    @Bean
    public ActionStateHolder actionStateHolder(ScheduleService scheduleService) {
        return new ActionStateHolder(scheduleService);
    }

    @Bean
    public SignupUser signupHospital(ScheduleService scheduleService) {
        return new SignupUser(scheduleService);
    }

    @Bean
    public CheckoutUser checkoutUser(ScheduleService scheduleService) {
        return new CheckoutUser(scheduleService);
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
}
