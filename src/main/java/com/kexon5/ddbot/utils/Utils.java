package com.kexon5.ddbot.utils;

import com.kexon5.ddbot.models.hospital.HospitalRecord;
import com.kexon5.ddbot.utils.markup.BoldString;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public class Utils {

    public static final String YES = "Да";
    public static final String NO = "Нет";

    public static final String MAN = "Прекрасный парень";
    public static final String WOMAN = "Прелестная девушка";

    public static final ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder YES_NO =
            Utils.getReplyKeyboardMarkupBuilder(List.of(YES, NO));

    public static final ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder W_M =
            Utils.getReplyKeyboardMarkupBuilder(List.of(MAN, WOMAN));


    public static <T> ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder getReplyKeyboardMarkupBuilder(Collection<T> buttons) {
        return ReplyKeyboardMarkup.builder()
                .keyboard(buttons.stream().map(T::toString)
                                          .map(KeyboardButton::new)
                                          .map(b -> new KeyboardRow(List.of(b)))
                                          .toList())
                .resizeKeyboard(true)
                .oneTimeKeyboard(true);
    }

    public static <T> ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder getReplyKeyboardMarkupBuilder(T[] buttons) {
        return getReplyKeyboardMarkupBuilder(List.of(buttons));
    }

    public static InlineKeyboardMarkup getMenu(Collection<InlineKeyboardButton> buttons) {
        return InlineKeyboardMarkup.builder()
                                   .keyboard(buttons.stream()
                                                    .map(List::of)
                                                    .toList())
                                   .build();
    }

    public static void fillStringBuilder(StringBuilder sb, List<HospitalRecord> records) {
        LocalDate lastDate = null;

        for (HospitalRecord record : records) {
            if (!Objects.equals(lastDate, record.getLocalDate())) {
                lastDate = record.getLocalDate();
                sb.append("\n")
                  .append(new BoldString(lastDate.format(DateTimeFormatter.ofPattern("E, dd.MM"))))
                  .append("\n\n");
            }
            sb.append(record.toAdminString()).append("\n");
        }
    }

    public static InlineKeyboardButton getButton(String callbackData, String text) {
        return InlineKeyboardButton.builder()
                                   .text(text)
                                   .callbackData(callbackData)
                                   .build();
    }
}
