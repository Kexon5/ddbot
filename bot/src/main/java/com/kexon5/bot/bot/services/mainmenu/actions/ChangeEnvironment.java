package com.kexon5.bot.bot.services.mainmenu.actions;

import com.kexon5.bot.bot.elements.ActionElement;
import com.kexon5.bot.bot.states.ActionState;
import com.kexon5.common.utils.ButtonUtils;
import org.bson.Document;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiConsumer;

import static com.kexon5.common.utils.Constants.envs;

// TODO: 06.08.2023 maybe up this action to publisher? But it will require few if checks for work...
public class ChangeEnvironment extends ActionElement {

    public ChangeEnvironment(ActionState actionState,
                             String currentEnv) {
        super(actionState, ChangeEnvSteps.values());

        ChangeEnvSteps.envButtons = envs.stream()
                                        .filter(env -> !env.equals(currentEnv))
                                        .map(env -> InlineKeyboardButton.builder()
                                                                                    .text(env)
                                                                                    .callbackData(env)
                                                                                    .build())
                                        .toList();
    }

    @Override
    public BiConsumer<BaseAbilityBot, Update> postAction() {
        return (bot, update) -> {};
    }

    public enum ChangeEnvSteps implements ActionMessageState {
        SELECT_ENV {

            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, Document document) {
                builder.replyMarkup(ButtonUtils.getMenu(envButtons));
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document document) {
                return "Пожалуйста, выберите env";
            }

        };

        private static List<InlineKeyboardButton> envButtons;

    }
}
