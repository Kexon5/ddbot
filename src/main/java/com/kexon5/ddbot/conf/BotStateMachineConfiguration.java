package com.kexon5.ddbot.conf;

import com.google.common.collect.ImmutableSet;
import com.kexon5.ddbot.statemachine.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.statemachine.config.EnableStateMachine;
import org.springframework.statemachine.config.StateMachineConfigurerAdapter;
import org.springframework.statemachine.config.builders.*;
import org.springframework.statemachine.listener.StateMachineListener;

import java.util.HashSet;
import java.util.Set;

@Configuration
@EnableStateMachine
public class BotStateMachineConfiguration extends StateMachineConfigurerAdapter<BotState, Integer> {

    public static final BotState EXIT_MOCK = () -> "MOCK";

    private final Set<Configurable> statesSet = new ImmutableSet.Builder<Configurable>()
            .add(ServiceStateHolder.ServiceState.values())
            .add(ActionStateHolder.ActionState.values())
            .build();

    @Bean
    public StateMachineListener<BotState, Integer> listener() {
        return new BotStateMachineListenerAdapter();
    }

    @Override
    public void configure(StateMachineConfigBuilder<BotState, Integer> config) {
    }

    @Override
    public void configure(StateMachineModelConfigurer<BotState, Integer> model) {
    }

    @Override
    public void configure(StateMachineConfigurationConfigurer<BotState, Integer> config) throws Exception {
        config
                .withConfiguration()
                .autoStartup(true)
                .listener(listener());
    }

    @Override
    public void configure(StateMachineStateConfigurer<BotState, Integer> states) throws Exception {
        states.withStates()
                .initial(ServiceStateHolder.ServiceState.MAIN_MENU)
                .states(new HashSet<>(statesSet));

        for (Configurable st: statesSet) {
            st.configureStates(states);
        }
    }

    @Override
    public void configure(StateMachineTransitionConfigurer<BotState, Integer> transitions) throws Exception {
        for (Configurable st: statesSet) {
            st.configureEvents(transitions);
        }

        transitions.withExit()
                .source(EXIT_MOCK).target(ServiceStateHolder.ServiceState.MAIN_MENU);
    }
}
