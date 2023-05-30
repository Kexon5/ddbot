package com.kexon5.ddbot.bot.services;

import com.kexon5.ddbot.statemachine.Buttonable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.List;

import static com.kexon5.ddbot.bot.services.ActionState.*;

@Getter
@RequiredArgsConstructor
public enum ServiceState implements Buttonable {
    EDIT_HOSPITALS_MENU("✍🏻 Редактирование ОПК", Collections.EMPTY_LIST, List.of(ADD_HOSPITAL, EDIT_HOSPITAL)),
    HOSPITALS_MENU("🏥 Настройка ОПК", List.of(EDIT_HOSPITALS_MENU), List.of(CREATE_SCHEDULE, READ_SCHEDULE, OPEN_REGISTRATION)),
    MAIN_MENU("🏡 Вернуться в главное меню", List.of(HOSPITALS_MENU), List.of(CHECK_IN_USER, CHECK_OUT_USER, SIGN_UP_USER));

    private final String buttonText;
    private final List<ServiceState> servicesList;
    private final List<ActionState> actionsList;

}
