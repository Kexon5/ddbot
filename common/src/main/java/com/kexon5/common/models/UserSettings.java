package com.kexon5.common.models;

import com.kexon5.common.statemachine.ButtonFactoryUtils;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.List;

@Getter
@Setter
@Builder
public class UserSettings {

    @Getter
    private static final List<Setting> defaultSettingsList = List.of(
            new Setting("Получать уведомления"),
            new Setting("Получать сообщения о новых выездах", false)
    );

    private List<Setting> settings;


    private void addNewSettings(int fromIndex) {
        for (int i = fromIndex; i < defaultSettingsList.size(); i++) {
            settings.add(defaultSettingsList.get(i));
        }
    }

    public List<InlineKeyboardButton> getButtons(List<String> callBackData, Runnable saveIfNeeded) {
        if (settings.size() != defaultSettingsList.size()) {
            addNewSettings(settings.size());
            saveIfNeeded.run();
        }

        return ButtonFactoryUtils.getButtons(settings, callBackData);
    }

    public boolean changeSetting(int index) {
        return settings.get(index).inverseWork();
    }

    public static UserSettings getDefault() {
        return new UserSettings(defaultSettingsList);
    }

    public boolean isNotificationEnabled() {
        return settings.get(0).isEnabled();
    }

}
