package com.kexon5.bot.bot.services.schedule.actions;

import com.kexon5.bot.bot.elements.ActionElement;
import com.kexon5.bot.bot.states.ActionState;
import com.kexon5.bot.services.RepositoryService;
import lombok.RequiredArgsConstructor;
import org.bson.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

public class CreateSchedule extends ActionElement {

    public CreateSchedule(ActionState actionState,
                          Predicate<Long> predicate,
                          RepositoryService repositoryService) {
        super(actionState, predicate, CreateSteps.values());

        CreateSteps.repositoryService = repositoryService;
    }

    @RequiredArgsConstructor
    public enum CreateSteps implements ActionMessageState {
        CREATED() {
            @Override
            public void initAction(long userId, Document userDocument) {
                userDocument.append(LINK, repositoryService.getSchedule().getLink());
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

        private static RepositoryService repositoryService;

    }

}
