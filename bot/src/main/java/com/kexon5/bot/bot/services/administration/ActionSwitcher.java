package com.kexon5.bot.bot.services.administration;

import com.kexon5.bot.bot.elements.InteractiveMenuElement;
import com.kexon5.bot.bot.states.ServiceState;
import com.kexon5.bot.models.ElementSetting;
import com.kexon5.bot.repositories.ElementSettingRepository;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.kexon5.common.statemachine.ButtonFactoryUtils.buttonFactoryFromButtonable;

public class ActionSwitcher extends InteractiveMenuElement {
    public ActionSwitcher(ServiceState serviceState,
                          ElementSettingRepository settingRepository) {
        this(serviceState, settingRepository, settingRepository.getActionSettings());
    }

    public ActionSwitcher(ServiceState serviceState,
                          ElementSettingRepository settingRepository,
                          List<ElementSetting> actionSettings) {
        super(serviceState,
              settingRepository.getBuilders(actionSettings, serviceState),
              buttonFactoryFromButtonable(actionSettings)
        );
    }

    @Override
    public String getAnswer(long userId, @Nullable String userText) {
        return "Включить/отключить действия";
    }
}