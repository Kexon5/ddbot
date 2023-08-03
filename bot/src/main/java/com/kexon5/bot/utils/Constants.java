package com.kexon5.bot.utils;

import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;

import java.util.List;

public class Constants {

    public static final String YES = "Да";
    public static final String NO = "Нет";

    public static final String MAN = "Прекрасный парень";
    public static final String WOMAN = "Прелестная девушка";

    public static final ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder YES_NO =
            ButtonUtils.getReplyKeyboardMarkupBuilder(List.of(YES, NO));

    public static final ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder W_M =
            ButtonUtils.getReplyKeyboardMarkupBuilder(List.of(MAN, WOMAN));
}
