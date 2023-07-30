package com.kexon5.common.statemachine;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;

public class ButtonFactoryUtils {

    public static InteractiveButtonFactory buttonFactoryFromButtonable(List<? extends Buttonable> buttonableList) {
        return (userId, callbackData) -> getButtons(buttonableList, callbackData);
    }

    public static List<InlineKeyboardButton> getButtons(List<? extends Buttonable> buttonableList,
                                                        List<String> callbackData) {
        List<InlineKeyboardButton> buttons = new ArrayList<>();
        for (int i = 0; i < callbackData.size(); i++) {
            buttons.add(InlineKeyboardButton.builder()
                                            .callbackData(callbackData.get(i))
                                            .text(buttonableList.get(i).getButtonText())
                                            .build());
        }
        return buttons;
    }
}
