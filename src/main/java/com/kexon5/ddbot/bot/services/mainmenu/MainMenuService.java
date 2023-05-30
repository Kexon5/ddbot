package com.kexon5.ddbot.bot.services.mainmenu;

import com.kexon5.ddbot.bot.services.ServiceElement;
import com.kexon5.ddbot.bot.services.ServiceState;
import com.kexon5.ddbot.repositories.UserRepository;
import com.kexon5.ddbot.statemachine.DialogueFlow;

import javax.annotation.Nullable;

import static org.telegram.abilitybots.api.objects.Flag.CALLBACK_QUERY;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

public class MainMenuService extends ServiceElement {

    private final UserRepository userRepository;

    public MainMenuService(UserRepository userRepository) {
        super(ServiceState.MAIN_MENU);

        this.userRepository = userRepository;
    }

    @Override
    public String getAnswer(long userId, @Nullable String userText) {
        return "Добро пожаловать, путник!";
    }

    @Override
    public DialogueFlow.DialogueFlowBuilder setAdditional(DialogueFlow.DialogueFlowBuilder builder) {
        return builder.onlyIf(update -> userRepository.existsByUserId(getChatId(update)))
                .onlyIf(update -> builder.getUserStateId(update) == -1
                        || (CALLBACK_QUERY.test(update) && update.getCallbackQuery().getData().equals(ServiceState.MAIN_MENU.toString())));
    }

}
