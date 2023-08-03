package com.kexon5.bot.utils;

import java.time.Duration;
import java.time.LocalDate;

public class DateUtils {

    private static final int MONTH_FOR_SPRING_EVENT = 4;

    private static String currentDirectory;

    private DateUtils() {}


    public static String getCurrentDirectory() {
        if (currentDirectory != null) return currentDirectory;

        LocalDate date = LocalDate.now();

        long daysToSpringAct = getDaysBetween(date, LocalDate.of(date.getYear(), MONTH_FOR_SPRING_EVENT, 15));
        currentDirectory = (daysToSpringAct > 0 ? "Весна'" : "Осень'") + date.getYear() % 100;
        return currentDirectory;
    }


    private static long getDaysBetween(LocalDate first, LocalDate second) {
        return Duration.between(first.atStartOfDay(), second.atStartOfDay()).toDays();
    }


}
