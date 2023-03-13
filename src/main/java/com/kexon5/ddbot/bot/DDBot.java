package com.kexon5.ddbot.bot;

import com.kexon5.ddbot.statemachine.ActionStateHolder;
import com.kexon5.ddbot.statemachine.BotState;
import com.kexon5.ddbot.statemachine.BotStateMachineInterceptor;
import com.kexon5.ddbot.statemachine.ServiceStateHolder;
import com.kexon5.ddbot.utils.Utils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHeaders;
import org.springframework.statemachine.StateMachine;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.kexon5.ddbot.statemachine.ServiceStateHolder.ServiceState.MAIN_MENU;
import static com.kexon5.ddbot.utils.Utils.ID;
import static com.kexon5.ddbot.utils.Utils.IS_MSG;
import static java.lang.Boolean.TRUE;

@Slf4j
@Getter
@RequiredArgsConstructor
public class DDBot extends TelegramLongPollingBot {
    private static final int RECONNECT_PAUSE = 10_000;
    private final StateMachine<BotState, Integer> stateMachine;

    private final String botUsername;
    private final String botToken;

    private Integer lastMenuMsg;

    public void init() {
        stateMachine.getStateMachineAccessor()
                    .doWithAllRegions(sa -> sa.addStateMachineInterceptor(new BotStateMachineInterceptor()
                                                                                    .setBotExecution(this::execute)));
    }

    @Override
    public void onUpdateReceived(Update update) {
        Optional.ofNullable(Utils.getMessageFromUpdate(update))
                .ifPresent(msg -> {
                    BotState currentState = stateMachine.getState().getId();

                    if (needResendMainMenu(currentState, msg)) {
                        execute(currentState, msg);
                    } else {
                        stateMachine.sendEvent(Mono.just(msg)).subscribe();
                    }
                });
    }

    public void execute(BotState state, Message<Integer> msg) {
        try {
            for (var m : getBotAnswer(state, msg)) {
                if (m instanceof SendMessage && state instanceof ServiceStateHolder.ServiceState) {
                    lastMenuMsg = execute((SendMessage)m).getMessageId();
                } else {
                    execute(m);
                }
            }
            if (state instanceof ActionStateHolder.ActionState) {
                lastMenuMsg = null;
            }
        } catch (TelegramApiException e) {
            log.error("Request handle error: ", e);
        }
    }

    private List<BotApiMethod<? extends Serializable>> getBotAnswer(BotState state, Message<Integer> msg) {
        List<BotApiMethod<? extends Serializable>> answerList = new ArrayList<>();
        if (state == MAIN_MENU) {
            MessageHeaders msgHeaders = msg.getHeaders();
            boolean isMsg = msgHeaders.get(IS_MSG, Boolean.class);
            long userId = msgHeaders.get(ID, Long.class);
            if (isMsg && lastMenuMsg != null) {
                answerList.add(DeleteMessage.builder()
                        .chatId(userId)
                        .messageId(lastMenuMsg)
                        .build());
            }

            answerList.addAll(state.getFinalMessage(msg).msgList());
            return answerList;
        }

        answerList.addAll(state.getMessage(msg).msgList());
        return answerList;
    }

    public boolean needResendMainMenu(BotState state, Message<Integer> msg) {
        return state == MAIN_MENU && TRUE.equals(msg.getHeaders().get(IS_MSG, Boolean.class));
    }

    public void botConnect(TelegramBotsApi telegramBotsApi) {
        try {
            telegramBotsApi.registerBot(this);
            log.info("Bot registered!");
        } catch (TelegramApiException e) {
            log.error("Try to reconnect after " + RECONNECT_PAUSE / 1000 + "sec. Error: " + e.getMessage());
            try {
                Thread.sleep(RECONNECT_PAUSE);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                return;
            }
            botConnect(telegramBotsApi);
        } catch (Exception e) {
            log.error("Unknown error: ", e);
        }
    }
}