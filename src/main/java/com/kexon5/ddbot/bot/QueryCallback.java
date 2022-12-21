package com.kexon5.ddbot.bot;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum QueryCallback {

    MAIN_MENU("mainMenu"),
    ONE_MIN("1min"),
    THIRTY_SEC("30sec"),
    ONE_HOUR("1hour"),
    LIST_OF_REMINDERS("reminders"),
    STOP_TIMER("stopTimer");

    private final String data;

}
