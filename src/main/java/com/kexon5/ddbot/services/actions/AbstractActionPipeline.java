package com.kexon5.ddbot.services.actions;

import com.kexon5.ddbot.services.messages.Message;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.kexon5.ddbot.utils.Utils.getId;

@RequiredArgsConstructor
public class AbstractActionPipeline implements ActionPipeline {
    private final List<ActionStep> actionSteps;

    @Override
    public Message test(int state, Update update) {
        return Optional.ofNullable(actionSteps.get(state).validator().apply(update.getMessage().getText()))
                .map(msg -> new Message(SendMessage.builder()
                        .chatId(getId(update))
                        .text(msg)
                        .build()))
                .orElse(null);
    }

    @Override
    public int size() {
        return actionSteps.size();
    }

    @Override
    public Message getMessage(int state, Update update) {
        return new Message(getMessages(state, update));
    }

    @Override
    public Message getFinalMessage(Update update) {
        return new Message(getMessages(actionSteps.size() - 1, update), true);
    }

    public List<BotApiMethod<? extends Serializable>> getMessages(int state, Update update) {
        List<BotApiMethod<? extends Serializable>> msgs = new ArrayList<>();
        Optional.ofNullable(needDeleteMenu(state, update)).ifPresent(msgs::add);
        msgs.addAll(actionSteps.get(state).messageCreator().getMessage(update));
        return msgs;
    }

    private DeleteMessage needDeleteMenu(int state, Update update) {
        return state != 0
                ? null
                : DeleteMessage.builder()
                .chatId(getId(update))
                .messageId(update.getCallbackQuery().getMessage().getMessageId())
                .build();
    }

}
