package com.kexon5.bot.services;

import com.kexon5.bot.models.hospital.HospitalRecord;

import java.time.LocalDate;
import java.util.List;

public interface NotificationSubscriber {

    void subscribe(List<HospitalRecord> records, LocalDate recordsDate);

    HospitalRecord.RecordState getSubscriberType();
}
