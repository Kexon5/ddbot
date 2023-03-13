package com.kexon5.ddbot.statemachine;

import lombok.extern.slf4j.Slf4j;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.listener.StateMachineListenerAdapter;
import org.springframework.statemachine.state.State;

@Slf4j
public class BotStateMachineListenerAdapter extends StateMachineListenerAdapter<BotState, Integer> {

    @Override
    public void stateChanged(State<BotState, Integer> from, State<BotState, Integer>  to) {
        log.info("State change to {}", to.getId());
    }

    @Override
    public void stateMachineError(StateMachine<BotState, Integer> stateMachine, Exception exception) {
        log.error("Some error: ", exception);
    }
}
