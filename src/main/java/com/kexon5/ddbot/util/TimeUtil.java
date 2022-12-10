package com.kexon5.ddbot.util;

import com.kexon5.ddbot.exceptions.IllegalTimeInput;

import java.util.List;

public class TimeUtil {
    private final static int SEC_IN_MIN = 60;
    private final static int SEC_IN_HOUR = 3600;

    public static int parseStringTimeToIntSec(String text) throws IllegalTimeInput {
        int sec = 0;
        List<String> timeIntervals =List.of(text.split(" "));

        if (!text.toLowerCase().matches("(([1-9]|1\\d|2[0-4])ч)?(([1-9]|[1-5]\\d|60)м)?(([1-9]|[1-5]\\d|60)с)?")
                || text.isBlank()) {
            throw new IllegalTimeInput("Неверный формат ввода времени");
        }

        for (String interval : timeIntervals) {
            long digits = Long.parseLong(interval.substring(0, interval.length() - 1));

            switch (interval.charAt(interval.length() - 1)) {
                case 'с' -> sec += digits;
                case 'м' -> sec += digits * SEC_IN_MIN;
                case 'ч' -> sec += digits * SEC_IN_HOUR;
            }
        }

        return sec;
    }
}
