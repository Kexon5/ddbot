package com.kexon5.ddbot.bot.states;

import com.kexon5.ddbot.models.Roles;
import com.kexon5.ddbot.models.User;
import com.kexon5.ddbot.statemachine.Buttonable;
import lombok.Getter;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import static com.kexon5.ddbot.bot.states.ActionState.*;

@Getter
public enum ServiceState implements Buttonable {
    EDIT_HOSPITALS_MENU(
            "✍🏻Редактирование ОПК",
            Roles.ADMIN,
            Collections.EMPTY_LIST,
            List.of(ADD_HOSPITAL, EDIT_HOSPITAL)
    ),
    HOSPITALS_MENU(
            "🏥Настройка записей в ОПК",
            Roles.HEAD,
            List.of(EDIT_HOSPITALS_MENU),
            List.of(OPEN_REGISTRATION)
    ),
    SCHEDULE_MENU(
            "📝Настройка расписания",
            Roles.MAIN_HEAD,
            Collections.EMPTY_LIST,
            List.of(CREATE_SCHEDULE, READ_SCHEDULE)
    ),
    SERVICE_SWITCHER_MENU(
            "⚙️Настройка сервисов",
            Roles.ADMIN,
            Collections.EMPTY_LIST,
            Collections.EMPTY_LIST
    ),
    ACTION_SWITCHER_MENU(
            "⚙️Настройка действий",
            Roles.ADMIN,
            Collections.EMPTY_LIST,
            Collections.EMPTY_LIST
    ),
    ADMINISTRATION_MENU(
            "⚙️Администрирование",
            Roles.HEAD,
//            Collections.EMPTY_LIST,
            List.of(SERVICE_SWITCHER_MENU, ACTION_SWITCHER_MENU),
            List.of(GRANT_ROLES)
    ),
    MAIN_MENU(
            "🏡Вернуться в главное меню",
            Roles.DONOR,
            List.of(HOSPITALS_MENU, SCHEDULE_MENU, ADMINISTRATION_MENU),
            List.of(CHECK_IN_USER, CHECK_OUT_USER, SIGN_UP_USER)
    );

    private final String buttonText;
    private final Function<User, Boolean> accessPredicate;
    private final List<ServiceState> servicesList;
    private final List<ActionState> actionsList;

    ServiceState(String buttonText, Roles role, List<ServiceState> servicesList, List<ActionState> actionsList) {
        this.buttonText = buttonText;
        this.accessPredicate = user -> user.getRoles().contains(role);
        this.servicesList = servicesList;
        this.actionsList = actionsList;
    }

}
