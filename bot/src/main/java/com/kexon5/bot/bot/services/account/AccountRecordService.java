package com.kexon5.bot.bot.services.account;

import com.kexon5.bot.bot.elements.MenuElement;
import com.kexon5.bot.bot.states.ServiceState;
import com.kexon5.bot.repositories.HospitalRecordRepository;
import com.kexon5.bot.utils.markup.MarkupList;
import com.kexon5.common.repositories.UserRepository;
import org.apache.commons.lang3.tuple.MutableTriple;

import javax.annotation.Nullable;

public class AccountRecordService extends MenuElement {

    private final UserRepository userRepository;
    private final HospitalRecordRepository hospitalRecordRepository;

    public AccountRecordService(ServiceState state,
                                UserRepository userRepository,
                                HospitalRecordRepository hospitalRecordRepository) {
        super(state);

        this.userRepository = userRepository;
        this.hospitalRecordRepository = hospitalRecordRepository;
    }

    @Override
    public String getAnswer(long userId, @Nullable String userText) {
        var userRecords = userRepository.findByUserId(userId).getRecords();
        if (userRecords != null && !userRecords.isEmpty()) {
            var recordIds = userRecords.stream()
                                   .map(MutableTriple::getLeft)
                                   .toList();

            var records = hospitalRecordRepository.findAllById(recordIds);
            MarkupList<String> recordList = new MarkupList<>();
            for (int i = 0; i < recordIds.size(); i++) {
                var addition = userRecords.get(i).getRight().isEmpty() ? "" : "\nПримечание: " + userRecords.get(i).getRight() + "\n";
                recordList.add(records.get(i).toCommonString() + addition);
            }

            return "Информация о Ваших выездах:\n\n" + recordList;

        }

        return "Хм, Вы ещё ни разу никуда не выезжали...\nСамое время это исправить!";
    }
}