package com.kexon5.ddbot.statemachine;

import org.springframework.statemachine.config.builders.StateMachineStateConfigurer;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;

public interface Configurable extends BotState {

    default void configureStates(StateMachineStateConfigurer<BotState, Integer> states) throws Exception {}

    void configureEvents(StateMachineTransitionConfigurer<BotState, Integer> transitions) throws Exception;
}
