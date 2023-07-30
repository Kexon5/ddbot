package com.kexon5.common.statemachine;

import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static org.telegram.abilitybots.api.objects.Flag.CALLBACK_QUERY;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

public class ButtonReply extends Reply {

    public ButtonReply(Predicate<Update> condition, BiConsumer<BaseAbilityBot, Update> action, String name) {
        super(List.of(condition), action, name);
    }

    public static ButtonReplyBuilder builder(String elementHolder, int index) {
        return new ButtonReplyBuilder(elementHolder, index);
    }

    public static class ButtonReplyBuilder {

        private final Predicate<Update> condition;

        private final String name;

        private final int buttonIndex;

        private BiConsumer<BaseAbilityBot, Update> action;
        private BiConsumer<Integer, Long> buttonChange;

        public ButtonReplyBuilder(String elementHolder, int buttonIndex) {
            this.buttonIndex = buttonIndex;

            this.name = buttonIndex + elementHolder;
            this.condition = update -> CALLBACK_QUERY.test(update)
                    && update.getCallbackQuery().getData().equals(name);
        }

        public ButtonReplyBuilder action(BiConsumer<BaseAbilityBot, Update> action) {
            this.action = action;
            return this;
        }

        public ButtonReplyBuilder buttonChange(BiConsumer<Integer, Long> buttonChange) {
            this.buttonChange = buttonChange;
            return this;
        }


        public ButtonReply build() {
            if (buttonChange == null)
                buttonChange = (index, id) -> {};

            if (action == null)
                action = (bot, upd) -> {};

            BiConsumer<BaseAbilityBot, Update> statefulAction = (bot, upd) -> {

                buttonChange.accept(buttonIndex, getChatId(upd));
                action.accept(bot, upd);
            };

            return new ButtonReply(condition, statefulAction, name);
        }
    }

}
