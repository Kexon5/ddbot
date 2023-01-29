package com.kexon5.ddbot.services;

import com.kexon5.ddbot.services.actions.Action;
import com.kexon5.ddbot.services.messages.Message;
import com.kexon5.ddbot.utils.Utils;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.Serializable;
import java.util.List;

import static com.kexon5.ddbot.utils.Utils.getId;

public class MainMenuService extends AbstractService {

    public MainMenuService(List<Service> innerServices, List<Action> innerActions) {
        super(innerServices, innerActions);
    }

    @Override
    public String getButtonText() {
        return "<-- Back to main menu";
    }

    @Override
    public String getMainMessage(Update update) {
        return "Darova yopta";
    }

    @Override
    public InlineKeyboardButton getMainMenuButton() {
        return null;
    }

    public Message getMenu(Update update) {
        BotApiMethod<? extends Serializable> msg;
        if (update.hasCallbackQuery()) {
                    msg = EditMessageText.builder()
                            .chatId(getId(update))
                            .text(getMainMessage(update))
                            .replyMarkup(Utils.getMenu(getButtonsList()))
                            .messageId(update.getCallbackQuery().getMessage().getMessageId())
                            .build();
        } else {
            msg = SendMessage.builder()
                    .chatId(getId(update))
                    .text(getMainMessage(update))
                    .replyMarkup(Utils.getMenu(getButtonsList()))
                    .build();
        }
        return new Message(List.of(msg));
    }
}
