package com.kexon5.ddbot.services;

import com.kexon5.ddbot.services.actions.Action;
import com.kexon5.ddbot.buttons.ButtonGenerator;
import com.kexon5.ddbot.services.messages.Message;
import com.kexon5.ddbot.utils.Utils;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.kexon5.ddbot.utils.Utils.getId;
@RequiredArgsConstructor
public abstract class AbstractService implements Service {

    protected final List<Service> innerServices;
    protected final List<Action> innerActions;

    @Getter
    protected final InlineKeyboardButton button = ButtonGenerator.of(this.getButtonText(), this::getMenu);

    public AbstractService() {
        this.innerServices = Collections.emptyList();
        this.innerActions = Collections.emptyList();
    }

    @Override
    public InlineKeyboardButton getMainMenuButton() {
        return ButtonGenerator.getMainButton();
    }

    @Override
    public Message getMenu(Update update) {
        int id = update.hasCallbackQuery() ? update.getCallbackQuery().getMessage().getMessageId() : update.getMessage().getMessageId();
        return new Message(List.of(EditMessageText.builder()
                .chatId(getId(update))
                .text(getMainMessage(update))
                .replyMarkup(Utils.getMenu(getButtonsList()))
                .messageId(id)
                .build()));
    }

    protected List<InlineKeyboardButton> getButtonsList() {
        List<InlineKeyboardButton> buttonsList = new ArrayList<>(getInternalServices());
        buttonsList.addAll(getActions());
        Optional.ofNullable(getMainMenuButton()).ifPresent(buttonsList::add);
        return buttonsList;
    }

    @Override
    public List<InlineKeyboardButton> getInternalServices() {
        return innerServices.stream()
                .map(Service::getButton)
                .toList();
    }

    @Override
    public List<InlineKeyboardButton> getActions() {
        return innerActions.stream()
                .map(Action::getButton)
                .toList();
    }
}
