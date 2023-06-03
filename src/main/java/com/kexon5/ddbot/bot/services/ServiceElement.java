package com.kexon5.ddbot.bot.services;

import com.kexon5.ddbot.statemachine.*;
import com.kexon5.ddbot.utils.Utils;
import lombok.Getter;
import org.springframework.data.util.Pair;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import javax.annotation.Nullable;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.kexon5.ddbot.bot.services.ActionState.BACK;
import static com.kexon5.ddbot.bot.services.ServiceState.MAIN_MENU;

@Getter
public abstract class ServiceElement extends MenuElement implements MessageState {

    protected final ServiceState serviceState;

    private final List<ServiceState> subServices;
    private final List<ActionState> subActions;

    private final List<Pair<Predicate<Long>, InlineKeyboardButton>> buttons = new ArrayList<>();

    protected ServiceElement(ServiceState serviceState) {
        this(serviceState, null);
    }

    protected ServiceElement(ServiceState serviceState,
                             @Nullable Predicate<Long> accessPredicate) {
        super(serviceState, accessPredicate);

        this.serviceState = serviceState;
        this.subServices = serviceState.getServicesList();
        this.subActions = serviceState.getActionsList();
    }

    @Override
    public DialogueFlow.DialogueFlowBuilder createReplyFlow() {
        DialogueFlow.DialogueFlowBuilder builder = DialogueFlow.builder(dbContext)
                                                               .enableStats(serviceState.name())
                                                               .action((bot, update) -> bot.silent().execute(getMessage(update)));

        for (ServiceState subService : subServices) {
            ServiceElement serviceElement = (ServiceElement) registry.get(subService);

            builder.next(serviceElement.getReplyFlowBuilder().build());

            buttons.add(getSecuredButton(subService.toString(), serviceElement));
            serviceElement.buttons.add(getButton(serviceState.name(), BACK));

            if (!MAIN_MENU.getServicesList().contains(subService) && !subService.equals(MAIN_MENU)) {
                serviceElement.buttons.add(getButton(MAIN_MENU.toString(), MAIN_MENU));
            }
        }

        for (ActionState subAction : subActions) {
            MenuElement actionElement = registry.get(subAction);

            buttons.add(getSecuredButton(subAction.toString(), actionElement));
        }

        return builder;
    }

    private static Pair<Predicate<Long>, InlineKeyboardButton> getButton(String callbackData, Buttonable actionState) {
        return Pair.of(userId -> true, Utils.getButton(callbackData, actionState.getButtonText()));
    }

    private static Pair<Predicate<Long>, InlineKeyboardButton> getSecuredButton(String callbackData, Accessable accessableElement) {
        return Pair.of(accessableElement.hasAccess(), Utils.getButton(callbackData, accessableElement.getButtonText()));
    }

    @Override
    public BotApiMethod<? extends Serializable> getMessage(Long userId, Integer msgId, @Nullable String userText) {
        return userText != null
                ? SendMessage.builder()
                          .chatId(userId)
                          .text(getAnswer(userId, userText))
                          .parseMode("Markdown")
                          .replyMarkup(getFilteredMenu(userId))
                          .build()
                : EditMessageText.builder()
                                 .chatId(userId)
                                 .text(getAnswer(userId, null))
                                 .replyMarkup(getFilteredMenu(userId))
                                 .parseMode("Markdown")
                                 .messageId(msgId)
                                 .build();
    }

    public InlineKeyboardMarkup getFilteredMenu(long userId) {
        return Utils.getMenu(buttons.stream()
                                    .filter(b -> b.getFirst().test(userId))
                                    .map(Pair::getSecond)
                                    .toList());
    }

}
