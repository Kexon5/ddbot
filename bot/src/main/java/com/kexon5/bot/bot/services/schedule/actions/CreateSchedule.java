package com.kexon5.bot.bot.services.schedule.actions;

import com.kexon5.bot.bot.elements.ActionElement;
import com.kexon5.bot.bot.states.ActionState;
import com.kexon5.bot.services.RepositoryService;
import lombok.RequiredArgsConstructor;
import org.bson.Document;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

import static com.kexon5.common.utils.StringUtils.escape;

public class CreateSchedule extends ActionElement {

    public CreateSchedule(ActionState actionState,
                          RepositoryService repositoryService) {
        super(actionState, CreateSteps.values());

        CreateSteps.repositoryService = repositoryService;
    }

    @RequiredArgsConstructor
    public enum CreateSteps implements ActionMessageState {
        CREATED() {
            @Override
            public void initAction(long userId, Document userDocument) {
                userDocument.append(LINK, repositoryService.getSchedule().getLink());
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
