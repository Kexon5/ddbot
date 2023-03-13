package com.kexon5.ddbot.statemachine;

import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.statemachine.StateContext;
import org.springframework.statemachine.StateMachine;
import org.springframework.statemachine.state.State;
import org.springframework.statemachine.support.StateMachineInterceptor;
import org.springframework.statemachine.transition.Transition;

import java.util.Optional;
import java.util.function.BiConsumer;

@Slf4j
public class BotStateMachineInterceptor implements StateMachineInterceptor<BotState, Integer> {

    @Setter
    @Accessors(chain = true)
    private BiConsumer<BotState, Message<Integer>> botExecution;
    private static Message<Integer> lastMessage;


    @Override
    public Message<Integer> preEvent(Message<Integer> message, StateMachine<BotState, Integer> stateMachine) {
        return message;
    }

    @Override
    public StateContext<BotState, Integer> preTransition(StateContext<BotState, Integer> stateContext) {
        return stateContext;
    }

    @Override
    public void preStateChange(State<BotState, Integer> state, Message<Integer> message,
                               Transition<BotState, Integer> transition, StateMachine<BotState, Integer> stateMachine,
                               StateMachine<BotState, Integer> rootStateMachine) {
    }

    @Override
    public StateContext<BotState, Integer> postTransition(StateContext<BotState, Integer> stateContext) {
        return stateContext;
    }

    @Override
    public void postStateChange(State<BotState, Integer> state, Message<Integer> message,
                                Transition<BotState, Integer> transition, StateMachine<BotState, Integer> stateMachine,
                                StateMachine<BotState, Integer> rootStateMachine) {
        BotState currentState = state.getId();
        message = Optional.ofNullable(message).orElse(lastMessage);
        botExecution.accept(currentState, message);
        lastMessage = message;
    }

    @Override
    public Exception stateMachineError(StateMachine<BotState, Integer> stateMachine,
                                       Exception exception) {
        log.error(exception.getMessage());
        return exception;
    }
}
