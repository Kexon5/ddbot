package com.kexon5.bot.services;

import com.kexon5.bot.models.hospital.HospitalRecord;
import com.kexon5.bot.repositories.HospitalRecordRepository;
import com.kexon5.common.models.User;
import com.kexon5.common.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
public class NotificationRecordService {

    private final HospitalRecordRepository hospitalRecordRepository;
    private final UserRepository userRepository;
    private final SilentSender sender;

    private final SendMessage.SendMessageBuilder firstNotification = SendMessage.builder()
                                                                                .text("Первая напоминалка")
                                                                                .parseMode("Markdown");

    private final SendMessage.SendMessageBuilder secondNotification = SendMessage.builder()
                                                                                 .text("Вторая напоминалка")
                                                                                 .parseMode("Markdown");


    @Scheduled(cron = "0 0 19 * * *")
    public void scheduledUpdateRecords() {
        updateOpenRecords();
        updateClosedRecords();
    }

    private void updateRecords(HospitalRecord.RecordState from,
                               HospitalRecord.RecordState to,
                               long hoursBreach,
                               SendMessage.SendMessageBuilder msgBuilder) {
        var records = hospitalRecordRepository.findAllByStateEquals(from);
        if (records == null || records.isEmpty()) return;

        var now = LocalDateTime.now();

        List<HospitalRecord> needToUpdateRecords = new ArrayList<>();

        long upperHoursBreach = hoursBreach + 24;

        records.forEach(record -> {
            long durationHours = Duration.between(now, record.getDate()).toHours();

            if (durationHours >= hoursBreach && durationHours < upperHoursBreach) {
                userRepository.findAllById(record.getUsers()).stream()
                              .filter(User::isNotificationEnabled)
                              .map(User::getUserId)
                              .map(id -> msgBuilder.chatId(id).build())
                              .forEach(msg -> sender.executeAsync(msg, c -> {}));
            } else if (durationHours < hoursBreach) {
                record.setState(to);
                needToUpdateRecords.add(record);
            }
        });

        if (!needToUpdateRecords.isEmpty()) {
            hospitalRecordRepository.saveAll(needToUpdateRecords);
        }
    }

    private void updateOpenRecords() {
        updateRecords(HospitalRecord.RecordState.OPEN, HospitalRecord.RecordState.CLOSED, 48, firstNotification);
    }

    private void updateClosedRecords() {
        updateRecords(HospitalRecord.RecordState.CLOSED, HospitalRecord.RecordState.OUTDATED, 0, secondNotification);
    }

}
