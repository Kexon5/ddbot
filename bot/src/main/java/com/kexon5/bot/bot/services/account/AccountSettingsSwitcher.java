package com.kexon5.bot.bot.services.account;

import com.kexon5.bot.bot.elements.InteractiveMenuElement;
import com.kexon5.bot.bot.states.ServiceState;
import com.kexon5.common.models.User;
import com.kexon5.common.models.UserSettings;
import com.kexon5.common.repositories.MailingGroupRepository;
import com.kexon5.common.repositories.UserRepository;
import com.kexon5.common.statemachine.ButtonReply;
import com.kexon5.common.statemachine.InteractiveButtonFactory;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.kexon5.common.services.MailingService.NEW_TABLES;

public class AccountSettingsSwitcher extends InteractiveMenuElement {

    public AccountSettingsSwitcher(ServiceState serviceState,
                                   UserRepository userRepository,
                                   MailingGroupRepository mailingGroupRepository) {
        super(serviceState, getButtonReplies(serviceState, userRepository, mailingGroupRepository), getButtonFactory(userRepository));
    }

    private static List<ButtonReply.ButtonReplyBuilder> getButtonReplies(ServiceState serviceState, UserRepository userRepository, MailingGroupRepository mailingGroupRepository) {
        AtomicInteger counter = new AtomicInteger();

        return UserSettings.getDefaultSettingsList().stream()
                           .map(setting -> (
                                   ButtonReply.builder(serviceState.name(), counter.getAndIncrement())
                                              .buttonChange((index, userId) -> {
                                                  var user = userRepository.findByUserId(userId);
                                                  changeSetting(mailingGroupRepository, user, index);

                                                  userRepository.save(user);
                                              })))
                           .toList();
    }

    private static void changeSetting(MailingGroupRepository mailingGroupRepository, User user, int index) {
        var settingEnabled = user.getUserSettings().changeSetting(index);
        if (index == 1) {
            var mailingGroup = mailingGroupRepository.findByGroupName(NEW_TABLES);
            if (settingEnabled) {
                mailingGroup.addUser(user.getUserId());
            } else {
                mailingGroup.removeUser(user.getUserId());
            }
            mailingGroupRepository.save(mailingGroup);
        }

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
