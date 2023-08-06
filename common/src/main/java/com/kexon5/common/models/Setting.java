package com.kexon5.common.models;

import com.kexon5.common.statemachine.Buttonable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Setting implements Buttonable {

    private String settingText;
    private boolean isEnabled;

    public Setting(String settingText) {
        this(settingText, true);
    }

    public Setting(String settingText, boolean isEnabled) {
        this.settingText = settingText;
        this.isEnabled = isEnabled;
    }

    public boolean inverseWork() {
        isEnabled = !isEnabled;
        return isEnabled;
    }

    public String getButtonText() {
        return settingText + ": " + (isEnabled ? "✅" : "❌");
    }
}
