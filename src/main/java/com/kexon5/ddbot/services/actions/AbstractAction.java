package com.kexon5.ddbot.services.actions;

import com.kexon5.ddbot.buttons.ButtonGenerator;
import com.kexon5.ddbot.services.messages.Message;
import com.kexon5.ddbot.services.hospitals.actions.ActionSteps;
import lombok.Getter;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.Arrays;
import java.util.List;

@Getter
public abstract class AbstractAction implements Action {

    private final ActionPipeline actionPipeline = new AbstractActionPipeline(initActionPipeline());
    private final InlineKeyboardButton button = ButtonGenerator.of(this.getButtonText(), this::handleRequest);
    private int state = 0;

    public abstract void actionResult();
    public abstract ActionSteps[] actionSteps();
    public List<ActionStep> initActionPipeline() {
        return Arrays.stream(actionSteps())
                .map(step -> new ActionStep(step::getMsg, step::handle))
                .toList();
    };

    @Override
    public Message handleRequest(Update update) {
        if (state > 0) {
            Message msg = actionPipeline.test(state - 1, update);
            if (msg != null) {
                return msg;
            }
        }
        if (((state + 1) % actionPipeline.size() == 0 && actionPipeline.size() != 1 && state != 0) || (actionPipeline.size() == 1 && state == 1)) {
            state = 0;
            actionResult();
            return actionPipeline.getFinalMessage(update);
        }
        return actionPipeline.getMessage(state++, update);
    }

}
