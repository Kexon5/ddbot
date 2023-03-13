package com.kexon5.ddbot.actions.hospitals;

import com.kexon5.ddbot.services.ScheduleService;
import com.kexon5.ddbot.statemachine.BotState;
import com.kexon5.ddbot.statemachine.Eventable;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class CreateSchedule {

    @RequiredArgsConstructor
    public enum CreateSteps implements BotState, Eventable {
        CREATED() {
            @Override
            public void init(long userId) {
                link = scheduleService.getSchedule().getLink();
            }

            @Override
            public String getMessageAnswer(String userText) {
                return Optional.ofNullable(link)
                        .map(link -> "Создана таблица для следующей акции.\n" +
                                "Как заполните таблицу, пожалуйста, вернитесь в сервис больничек - будет доступна кнопка для обработки Ваших записей.\n\n" + link)
                        .orElse("Где-то случился косяк... Пиши одмену!");
            }

            private String link;

        };

        private static ScheduleService scheduleService;

    }

    public CreateSchedule(ScheduleService scheduleService) {
        CreateSteps.scheduleService = scheduleService;
    }

}
