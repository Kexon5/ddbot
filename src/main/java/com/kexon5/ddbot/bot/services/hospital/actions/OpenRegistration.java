package com.kexon5.ddbot.bot.services.hospital.actions;

import com.kexon5.ddbot.bot.services.ActionElement;
import com.kexon5.ddbot.models.hospital.HospitalRecord;
import com.kexon5.ddbot.services.RepositoryService;
import lombok.RequiredArgsConstructor;
import org.bson.Document;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;

import static com.kexon5.ddbot.bot.services.ActionState.OPEN_REGISTRATION;
import static com.kexon5.ddbot.utils.Utils.*;

public class OpenRegistration extends ActionElement {

    public OpenRegistration(RepositoryService repositoryService) {
        super(OPEN_REGISTRATION, OpenSteps.values());

        OpenRegistration.OpenSteps.repositoryService = repositoryService;
    }

    @RequiredArgsConstructor
    public enum OpenSteps implements ActionMessageState {
        GET {
            @Override
            public void initAction(long userId, Document userDocument) {
                List<HospitalRecord> records = repositoryService.getAllRecords(HospitalRecord.RecordState.READY);

                userDocument.append("RECORDS", records);
            }

            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, Document document) {
                builder.replyMarkup(YES_NO.build());
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document document) {
                List<HospitalRecord> records = document.getList("RECORDS", HospitalRecord.class);

                StringBuilder sb = new StringBuilder();

                fillStringBuilder(sb, records);


                return sb.append("\nВы хотите открыть регистрацию для следующих записей?")
                         .toString();
            }

            @Override
            public String validate(long userId, String userText, Document document) {
                return userText.equals(YES) || userText.equals(NO)
                        ? userText
                        : null;
            }

            @Override
            public String errorText() {
                return "Да или нет...";
            }
        },
        ACCEPT {
            @Override
            public void finalAction(long userId, @Nullable String userText, Document document) {
                if (document.getString(GET.name()).equals(YES)) {
                    List<HospitalRecord> records = document.getList("RECORDS", HospitalRecord.class);
                    records.forEach(r -> r.setState(HospitalRecord.RecordState.OPEN));
                    repositoryService.saveRecords(records);
                }
            }

            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, Document document) {
                builder.replyMarkup(new ReplyKeyboardRemove(true));
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document document) {
                return document.getString(GET.name()).equals(YES)
                        ? "Успешно выполнено"
                        : "Успешно ничего не сделано";
            }
        };

        private static RepositoryService repositoryService;
    }
}
