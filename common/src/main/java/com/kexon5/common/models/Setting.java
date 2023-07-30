package com.kexon5.common.models;

import com.kexon5.common.statemachine.Buttonable;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Setting implements Buttonable {

    private String settingText;
    private boolean isEnabled = true;

    public Setting(String settingText) {
        this.settingText = settingText;
    }

    public void inverseWork() {
        isEnabled = !isEnabled;
    }

    public String getButtonText() {
        return settingText + ": " + (isEnabled ? "✅" : "❌");
    }
}
