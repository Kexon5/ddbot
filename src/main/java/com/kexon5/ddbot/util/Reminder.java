package com.kexon5.ddbot.util;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;

@NoArgsConstructor
@AllArgsConstructor
public class Reminder extends Timer {
    private Date detonationDate;
    private final SimpleDateFormat formatter = new SimpleDateFormat("hh:mm:ss dd/MM/yyyy");

    @Override
    public String toString() {
        return formatter.format(detonationDate);
    }
}
