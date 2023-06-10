package com.kexon5.ddbot.statemachine;

import com.kexon5.ddbot.utils.Utils;
import lombok.Getter;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.telegram.abilitybots.api.objects.Flag.CALLBACK_QUERY;

public class ButtonReply extends Reply {

    @Getter
    private final InlineKeyboardButton button;

    public ButtonReply(Predicate<Update> condition, BiConsumer<BaseAbilityBot, Update> action, InlineKeyboardButton button, String name) {
        super(List.of(condition), action, name);
        this.button = button;
    }

    public static ButtonReplyBuilder builder(String callbackData, String buttonText, boolean defaultValue) {
        return new ButtonReplyBuilder(callbackData, buttonText, defaultValue);
    }

    public static class ButtonReplyBuilder {

        private static final List<String> DEFAULT_POSSIBLE_VALUES = List.of("❌", "✅");
        private Predicate<Update> condition;
        private BiConsumer<BaseAbilityBot, Update> action;
        private Consumer<InlineKeyboardButton> buttonChange;
        private InlineKeyboardButton button;

        private String name;


        private String buttonText;

        private List<String> possibleValues;
        private int possibleValueIndex = 0;

        public ButtonReplyBuilder(String callbackData, String buttonText, boolean defaultValue) {
            button(callbackData, buttonText, defaultValue);
        }


        private int getIndex() {
            int size = possibleValues.size();
            int returnValue = possibleValueIndex++;

            possibleValueIndex %= size;

            return returnValue;
        }

        private String getText() {
            return buttonText + ": " + possibleValues.get(getIndex());
        }

        public ButtonReplyBuilder button(String callbackData, String buttonText) {
            return button(callbackData, buttonText, DEFAULT_POSSIBLE_VALUES);
        }

        public ButtonReplyBuilder button(String callbackData, String buttonText, boolean defaultValue) {
            return button(callbackData, buttonText, DEFAULT_POSSIBLE_VALUES, defaultValue ? 1 : 0);
        }

        public ButtonReplyBuilder button(String callbackData, String buttonText, List<String> possibleValues) {
            return button(callbackData, buttonText, possibleValues, 0);
        }

        public ButtonReplyBuilder button(String callbackData, String buttonText, List<String> possibleValues, int possibleValueIndex) {
            this.possibleValues = possibleValues;
            this.possibleValueIndex = possibleValueIndex;
            this.buttonText = buttonText;

            this.button = Utils.getButton(callbackData, getText());
            this.name = button.getCallbackData();
            this.condition = update -> CALLBACK_QUERY.test(update)
                    && update.getCallbackQuery().getData().equals(button.getCallbackData());

            return this;
        }

        public ButtonReplyBuilder action(BiConsumer<BaseAbilityBot, Update> action) {
            this.action = action;
            return this;
        }

        public ButtonReplyBuilder buttonChange(Consumer<InlineKeyboardButton> buttonChange) {
            this.buttonChange = buttonChange;
            return this;
        }

        public ButtonReply build() {
            if (buttonChange == null)
                buttonChange = button -> {};

            if (action == null)
                action = (bot, upd) -> {};

            BiConsumer<BaseAbilityBot, Update> statefulAction = (bot, upd) -> {
                button.setText(getText());

                buttonChange.accept(button);
                action.accept(bot, upd);
            };

            return new ButtonReply(condition, statefulAction, button, name);
        }
    }

}
