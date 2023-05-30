package com.kexon5.ddbot.bot.services.hospital.actions;

import com.kexon5.ddbot.bot.services.ActionElement;
import com.kexon5.ddbot.services.ScheduleService;
import lombok.RequiredArgsConstructor;
import org.bson.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

import static com.kexon5.ddbot.bot.services.ActionState.CREATE_SCHEDULE;

public class CreateSchedule extends ActionElement {

    public CreateSchedule(ScheduleService scheduleService) {
        super(CREATE_SCHEDULE, CreateSteps.values());

        CreateSteps.scheduleService = scheduleService;
    }

    @RequiredArgsConstructor
    public enum CreateSteps implements ActionMessageState {
        CREATED() {
            @Override
            public void initAction(long userId, Document userDocument) {
                userDocument.append(LINK, scheduleService.getSchedule().getLink());
            }

            @Nullable
            private static String escape(String text) {
                return text != null
                        ? text.replace("_", "\\_")
                              .replace("*", "\\*")
                              .replace("[", "\\[")
                              .replace("`", "\\`")
                        : null;
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return Optional.ofNullable(escape(userDocument.getString(LINK)))
                        .map(link -> "Создана таблица для следующей акции.\n" +
                                "Как заполните таблицу, пожалуйста, вернитесь в сервис больничек - будет доступна кнопка для обработки Ваших записей.\n\n" + link)
                        .orElse("Где-то случился косяк... Пиши одмену!");
            }

            private static final String LINK = "LINK";

        };

        private static ScheduleService scheduleService;

    }

}
