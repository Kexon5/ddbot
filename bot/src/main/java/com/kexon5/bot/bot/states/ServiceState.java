package com.kexon5.bot.bot.states;


import com.kexon5.common.models.Role;
import com.kexon5.common.models.User;
import com.kexon5.common.statemachine.Accessable;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

import static com.kexon5.bot.bot.states.ActionState.*;

@Getter
public enum ServiceState implements Accessable {
    EDIT_HOSPITALS_MENU(
            "✍🏻Редактирование ОПК",
            Role.ADMIN,
            Collections.EMPTY_LIST,
            List.of(ADD_HOSPITAL, EDIT_HOSPITAL)
    ),
    HOSPITALS_MENU(
            "🏥Настройка записей в ОПК",
            Role.HEAD,
            List.of(EDIT_HOSPITALS_MENU),
            List.of(OPEN_REGISTRATION)
    ),
    SCHEDULE_MENU(
            "📝Настройка расписания",
            Role.MAIN_HEAD,
            Collections.EMPTY_LIST,
            List.of(CREATE_SCHEDULE, READ_SCHEDULE)
    ),
    SERVICE_SWITCHER_MENU(
            "⚙️Настройка сервисов",
            Role.ADMIN,
            Collections.EMPTY_LIST,
            Collections.EMPTY_LIST
    ),
    ACTION_SWITCHER_MENU(
            "⚙️Настройка действий",
            Role.ADMIN,
            Collections.EMPTY_LIST,
            Collections.EMPTY_LIST
    ),
    ADMINISTRATION_MENU(
            "⚙️Администрирование",
            Role.HEAD,
//            Collections.EMPTY_LIST,
            List.of(SERVICE_SWITCHER_MENU, ACTION_SWITCHER_MENU),
            List.of(GRANT_ROLES, MAILING_BY_ROLE)
    ),
    ACCOUNT_SETTINGS_SWITCHER(
            "Мои настройки",
            Role.DONOR,
            Collections.EMPTY_LIST,
            Collections.EMPTY_LIST
    ),
    ACCOUNT_RECORD_MENU(
            "Информация о моих выездах",
            Role.DONOR,
            Collections.EMPTY_LIST,
            Collections.EMPTY_LIST
    ),
    ACCOUNT_MENU(
            "Настройка профиля",
            Role.DONOR,
            List.of(ACCOUNT_RECORD_MENU, ACCOUNT_SETTINGS_SWITCHER),
            Collections.EMPTY_LIST
    ),
    MAIN_MENU(
            "🏡Вернуться в главное меню",
            Role.DONOR,
            List.of(HOSPITALS_MENU, SCHEDULE_MENU, ADMINISTRATION_MENU, ACCOUNT_MENU),
            List.of(CHECK_IN_USER, CHECK_OUT_USER, SIGN_UP_USER)
    );

    private final String buttonText;
    private final Role accessRole;
    private final List<ServiceState> servicesList;
    private final List<ActionState> actionsList;

    ServiceState(String buttonText, Role role, List<ServiceState> servicesList, List<ActionState> actionsList) {
        this.buttonText = buttonText;
        this.accessRole = role;
        this.servicesList = servicesList;
        this.actionsList = actionsList;
    }

    @Override
    public boolean hasAccess(User user) {
        return user.getRoles().contains(accessRole);
    }
}
