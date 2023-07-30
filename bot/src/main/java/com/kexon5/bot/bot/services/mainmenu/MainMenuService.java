package com.kexon5.bot.bot.services.mainmenu;

import com.kexon5.bot.bot.elements.MenuElement;
import com.kexon5.bot.bot.states.ServiceState;
import com.kexon5.common.repositories.UserRepository;
import com.kexon5.common.statemachine.DialogueFlow;

import javax.annotation.Nullable;

import static org.telegram.abilitybots.api.objects.Flag.CALLBACK_QUERY;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

public class MainMenuService extends MenuElement {

    private final UserRepository userRepository;

    public MainMenuService(ServiceState state,
                           UserRepository userRepository) {
        super(state);

        this.userRepository = userRepository;
    }

    @Override
    public String getAnswer(long userId, @Nullable String userText) {
        return "Добро пожаловать, путник!";
    }

    @Override
    public DialogueFlow.DialogueFlowBuilder setAdditional(DialogueFlow.DialogueFlowBuilder builder) {
        return builder.onlyIf(update -> {
                          long id = getChatId(update);
                          return id < 400L || userRepository.existsByUserId(id);
                      })
                .onlyIf(update -> builder.getUserStateId(update) == -1
                        || (CALLBACK_QUERY.test(update) && update.getCallbackQuery().getData().equals(elementState.name())));
    }

}
