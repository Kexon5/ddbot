package com.kexon5.bot.bot.elements;

import com.kexon5.bot.bot.states.ServiceState;
import com.kexon5.bot.utils.Utils;
import com.kexon5.common.statemachine.Accessable;
import com.kexon5.common.statemachine.Buttonable;
import com.kexon5.common.statemachine.Element;
import com.kexon5.common.statemachine.MessageState;
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

public abstract class AbstractServiceElement extends Element<ServiceState> implements MessageState {

    protected final List<Pair<Predicate<Long>, InlineKeyboardButton>> buttons = new ArrayList<>();

    protected AbstractServiceElement(ServiceState serviceState) {
        super(serviceState);
    }

    public abstract BotApiMethod<? extends Serializable> getMessage(long userId,
                                                                    Integer msgId,
                                                                    @Nullable String userText);

    protected List<InlineKeyboardButton> getButtons(long userId) {
        return buttons.stream()
                      .filter(b -> b.getFirst().test(userId))
                      .map(Pair::getSecond)
                      .toList();
    }

    public InlineKeyboardMarkup getFilteredMenu(long userId) {
        return Utils.getMenu(getButtons(userId));
    }

    protected BotApiMethod<? extends Serializable> sendMessage(long userId, String userText) {
        return SendMessage.builder()
                          .chatId(userId)
                          .text(getAnswer(userId, userText))
                          .parseMode("Markdown")
                          .replyMarkup(getFilteredMenu(userId))
                          .build();
    }

    protected BotApiMethod<? extends Serializable> editMessage(long userId, Integer msgId) {
        return EditMessageText.builder()
                              .chatId(userId)
                              .text(getAnswer(userId, null))
                              .replyMarkup(getFilteredMenu(userId))
                              .parseMode("Markdown")
                              .messageId(msgId)
                              .build();
    }

    protected static Pair<Predicate<Long>, InlineKeyboardButton> getButton(String callbackData, Buttonable actionState) {
        return Pair.of(userId -> true, Utils.getButton(callbackData, actionState.getButtonText()));
    }

    protected static Pair<Predicate<Long>, InlineKeyboardButton> getSecuredButton(String callbackData, Accessable accessableElement) {
        return Pair.of(accessableElement.hasAccess(), Utils.getButton(callbackData, accessableElement.getButtonText()));
    }

}
