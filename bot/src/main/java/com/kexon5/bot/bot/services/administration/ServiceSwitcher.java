package com.kexon5.bot.bot.services.administration;

import com.kexon5.bot.bot.elements.InteractiveMenuElement;
import com.kexon5.bot.bot.states.ServiceState;
import com.kexon5.bot.models.ElementSetting;
import com.kexon5.bot.repositories.ElementSettingRepository;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.kexon5.common.statemachine.ButtonFactoryUtils.buttonFactoryFromButtonable;

public class ServiceSwitcher extends InteractiveMenuElement {
    public ServiceSwitcher(ServiceState serviceState,
                           ElementSettingRepository settingRepository) {
        this(serviceState, settingRepository, settingRepository.getServiceSettings());
    }

    public ServiceSwitcher(ServiceState serviceState,
                           ElementSettingRepository settingRepository,
                           List<ElementSetting> serviceSettings) {
        super(serviceState,
              settingRepository.getBuilders(serviceSettings, serviceState),
              buttonFactoryFromButtonable(serviceSettings));
    }

    @Override
    public String getAnswer(long userId, @Nullable String userText) {
        return "Включить/отключить сервисы";
    }
}
