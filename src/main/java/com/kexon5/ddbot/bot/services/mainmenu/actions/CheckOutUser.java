package com.kexon5.ddbot.bot.services.mainmenu.actions;

import com.kexon5.ddbot.bot.services.ActionElement;
import com.kexon5.ddbot.models.hospital.HospitalRecord;
import com.kexon5.ddbot.services.ScheduleService;
import com.kexon5.ddbot.utils.markup.BoldString;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static com.kexon5.ddbot.bot.services.ActionState.CHECK_OUT_USER;
import static com.kexon5.ddbot.utils.Utils.*;

public class CheckOutUser extends ActionElement {

    public CheckOutUser(ScheduleService scheduleService) {
        super(
                CHECK_OUT_USER,
                scheduleService::userHasActiveRecord,
                CheckOutSteps.values()
        );

        CheckOutSteps.scheduleService = scheduleService;
    }

    @RequiredArgsConstructor
    public enum CheckOutSteps implements ActionMessageState {
        STEP1 {
            @Override
            public void initAction(long userId, Document userDocument) {
                userDocument.append(RECORD, scheduleService.getUserActiveRecord(userId));
            }

            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, @Nonnull Document userDocument) {
                builder.replyMarkup(YES_NO.build());
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return "У Вас есть активная запись: " +
                        new BoldString(userDocument.get(RECORD, HospitalRecord.class).toCommonString()) +
                        "\n\nВы действительно хотите отменить запись?";
            }

            @Override
            public String validate(long userId, String userText, Document document) {
                return userText.equals(YES) || userText.equals(NO)
                        ? userText
                        : null;
            }

        },
        STEP2 {
            @Override
            public void finalAction(long userId, @Nullable String userText, Document userDocument) {
                if (userDocument.getString(STEP1.name()).equals(YES)) {
                    HospitalRecord record = userDocument.get(RECORD, HospitalRecord.class);
                    scheduleService.checkOutUser(record, userId);
                }
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return userDocument.getString(STEP1.name()).equals(YES)
                        ? "Ваша запись успешно удалена"
                        : "Успешно ничего не сделано";
            }

        };

        private static final String RECORD = "RECORD";

        private static ScheduleService scheduleService;

    }
}
