package com.kexon5.ddbot.statemachine;

import com.kexon5.ddbot.actions.hospitals.CheckoutUser;
import com.kexon5.ddbot.actions.hospitals.CreateSchedule;
import com.kexon5.ddbot.actions.hospitals.ReadSchedule;
import com.kexon5.ddbot.actions.hospitals.SignupUser;
import com.kexon5.ddbot.actions.hospitals.edit.AddHospital;
import com.kexon5.ddbot.actions.hospitals.edit.EditHospital;
import com.kexon5.ddbot.services.ScheduleService;
import com.kexon5.ddbot.utils.BotMessage;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;

import java.util.Set;
import java.util.function.Predicate;

import static com.kexon5.ddbot.conf.BotStateMachineConfiguration.EXIT_MOCK;

@Slf4j
public class ActionStateHolder {
    @RequiredArgsConstructor
    public enum ActionState implements Configurable, Buttonable {
        EDIT_HOSPITAL(EDIT_HOSPITAL_BUTTON, EditHospital.EditSteps.values()),
        ADD_HOSPITAL(ADD_HOSPITAL_BUTTON, AddHospital.AddSteps.values()),
        CREATE_SCHEDULE(CREATE_SCHEDULE_BUTTON, CreateSchedule.CreateSteps.values()),
        READ_SCHEDULE(READ_SCHEDULE_BUTTON, ReadSchedule.ReadSteps.values()),
        SIGNUP_USER(SIGNUP_USER_BUTTON, SignupUser.SignupSteps.values()) {
            @Override
            public Predicate<Long> hasAccess() {
                return userId -> !scheduleService.userHasActiveRecord(userId);
            }
        },
        CHECKOUT_USER(CHECKOUT_USER_BUTTON, CheckoutUser.CheckoutSteps.values()) {
            @Override
            public Predicate<Long> hasAccess() {
                return userId -> scheduleService.userHasActiveRecord(userId);
            }
        };

        @Getter
        private final String buttonText;
        private final BotState[] steps;

        private static ScheduleService scheduleService;

        @Override
        public void configureStates(StateMachineStateConfigurer<BotState, Integer> states) throws Exception {
            states.withStates()
                    .parent(this)
                    .initial(steps[0])
                    .state(steps[steps.length - 1], steps[steps.length - 1].action())
                    .exit(EXIT_MOCK)
                    .states(Set.of(steps));
        }

        @Override
        public void configureEvents(StateMachineTransitionConfigurer<BotState, Integer> transitions) throws Exception {
            transitions.withExternal().source(this).target(ServiceStateHolder.ServiceState.MAIN_MENU).event(-1);
            for (int i = 1; i < steps.length; i++) {
                transitions.withExternal()
                        .source(steps[i - 1]).target(steps[i])
                        .guard(steps[i - 1].guard())
                        .action(steps[i - 1].action())
                        .event(0);
            }
            transitions.withExternal()
                    .source(steps[steps.length - 1]).target(EXIT_MOCK)
                    .timerOnce(100);
        }

        @Override
        public BotMessage getMessage(long userId, int msgId, String userText, boolean isMsg) {
            return new BotMessage(DeleteMessage.builder()
                    .chatId(userId)
                    .messageId(msgId)
                    .build());
        }

        @Override
        public void validateError(Throwable ex) {
            log.error(ex.getMessage());
        }

        @Override
        public void actionError(Throwable ex) {
            log.error(ex.getMessage());
        }
    }

    public ActionStateHolder(ScheduleService scheduleService) {
        ActionState.scheduleService = scheduleService;
    }
}
