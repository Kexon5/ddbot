package com.kexon5.bot.utils;

import com.kexon5.bot.models.hospital.HospitalRecord;
import com.kexon5.bot.utils.markup.BoldString;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class HospitalUtils {

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
}
