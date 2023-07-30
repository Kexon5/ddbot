package com.kexon5.bot.bot.services.account;

import com.kexon5.bot.bot.elements.InteractiveMenuElement;
import com.kexon5.bot.bot.states.ServiceState;
import com.kexon5.common.models.UserSettings;
import com.kexon5.common.repositories.UserRepository;
import com.kexon5.common.statemachine.ButtonReply;
import com.kexon5.common.statemachine.InteractiveButtonFactory;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class AccountSettingsSwitcher extends InteractiveMenuElement {

    public AccountSettingsSwitcher(ServiceState serviceState,
                                   UserRepository userRepository) {
        super(serviceState, getButtonReplies(serviceState, userRepository), getButtonFactory(userRepository));
    }

    private static List<ButtonReply.ButtonReplyBuilder> getButtonReplies(ServiceState serviceState, UserRepository userRepository) {
        AtomicInteger counter = new AtomicInteger();

        return UserSettings.getDefaultSettingsList().stream()
                           .map(setting -> (
                                   ButtonReply.builder(serviceState.name(), counter.getAndIncrement())
                                              .buttonChange((index, userId) -> {
                                                  var user = userRepository.findByUserId(userId);
                                                  user.getUserSettings().changeSetting(index);

                                                  userRepository.save(user);
                                              })))
                           .toList();
    }

    private static InteractiveButtonFactory getButtonFactory(UserRepository userRepository) {
        return (userId, callbackData) -> {
            var user = userRepository.findByUserId(userId);
            var userSettings = user.getUserSettings();
            return userSettings.getButtons(callbackData, () -> userRepository.save(user));
        };
    }

    @Override
    public String getAnswer(long userId, @Nullable String userText) {
        return "Пользовательские настройки";
    }

}
