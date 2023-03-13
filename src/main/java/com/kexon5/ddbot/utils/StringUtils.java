package com.kexon5.ddbot.utils;

import com.kexon5.ddbot.models.hospital.HospitalRecord;
import com.kexon5.ddbot.utils.markup.BoldString;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

public class StringUtils {

    public static String getRecordsMessage(List<HospitalRecord> records, String title, Function<HospitalRecord, String> recordFormat, String ask) {
        StringBuilder sb = new StringBuilder()
                .append(title)
                .append(":\n");
        LocalDate lastDate = null;
        for (HospitalRecord record : records) {
            if (!Objects.equals(lastDate, record.getLocalDate())) {
                lastDate = record.getLocalDate();
                sb.append("\n")
                        .append(new BoldString(lastDate.format(DateTimeFormatter.ofPattern("E, dd.MM"))))
                        .append("\n\n");
            }
            sb.append(record.toCommonString()).append("\n");
        }
        sb.append("\n").append(new BoldString(ask));
        return sb.toString();
    }
}
