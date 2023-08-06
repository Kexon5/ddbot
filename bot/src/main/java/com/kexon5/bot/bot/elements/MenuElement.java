package com.kexon5.bot.bot.elements;

import com.kexon5.bot.bot.states.ActionState;
import com.kexon5.bot.bot.states.ServiceState;
import com.kexon5.common.statemachine.DialogueFlow;
import com.kexon5.common.statemachine.Element;
import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Message;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import static com.kexon5.bot.bot.states.ActionState.BACK;
import static com.kexon5.bot.bot.states.ServiceState.MAIN_MENU;

@Getter
public abstract class MenuElement extends AbstractServiceElement {

    private final List<ServiceState> subServices;
    private final List<ActionState> subActions;

    @Setter
    public static Map<Long, Message> historyMap;

    protected MenuElement(ServiceState serviceState) {
        super(serviceState);

        this.subServices = serviceState.getServicesList();
        this.subActions = serviceState.getActionsList();
    }

    @Override
    public DialogueFlow.DialogueFlowBuilder createReplyFlow() {
        DialogueFlow.DialogueFlowBuilder builder = DialogueFlow.builder(dbContext)
                                                               .enableStats(elementState.name())
                                                               .actionService(this::getMessage);

        for (ServiceState subService : subServices) {
            AbstractServiceElement element = (AbstractServiceElement) registry.get(subService);
            builder.next(element.getReplyFlowBuilder().build());

            buttons.add(getSecuredButton(subService.name(), element));
            element.buttons.add(getButton(elementState.name(), BACK));

            if (!MAIN_MENU.getServicesList().contains(subService) && !subService.equals(MAIN_MENU)) {
                element.buttons.add(getButton(MAIN_MENU.name(), MAIN_MENU));
            }

        }

        for (ActionState subAction : subActions) {
            Element actionElement = registry.get(subAction);

            buttons.add(getSecuredButton(subAction.name(), actionElement));
        }

        return builder;
    }

    @Override
    public BotApiMethod<? extends Serializable> getMessage(long userId, Integer msgId, @Nullable String userText, boolean isChangeEnv) {
        return userText != null || msgId == null || isChangeEnv
                ? sendMessage(userId, userText)
                : editMessage(userId, msgId);
    }

}
