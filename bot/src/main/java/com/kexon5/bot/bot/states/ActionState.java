package com.kexon5.bot.bot.states;


import com.kexon5.common.models.Role;
import com.kexon5.common.models.User;
import com.kexon5.common.statemachine.Accessable;
import lombok.Getter;

import java.util.function.Predicate;

@Getter
public enum ActionState implements Accessable {
    EDIT_HOSPITAL("✒️Отредактировать данные об ОПК", Role.ADMIN),
    ADD_HOSPITAL("➕Добавить ОПК", Role.ADMIN),
    CREATE_SCHEDULE("➕Создать расписание", Role.MAIN_HEAD),
    READ_SCHEDULE("💫Загрузить данные из расписания", Role.MAIN_HEAD),
    OPEN_REGISTRATION("🟢Открыть регистрацию", Role.HEAD),
    CHECK_IN_USER("✒️Записаться на выезд"),
    CHECK_OUT_USER("❌Отменить запись"),
    GRANT_ROLES("🔝Дать права пользователю", Role.ADMIN),
    MAILING_BY_ROLE("\uD83D\uDC8C Разослать сообщения", Role.HEAD),
    SIGN_UP_USER("TEST"),

    BACK( "🔙Назад");

    @Getter
    private final String buttonText;
    private final Role accessRole;

    ActionState(String buttonText) {
        this(buttonText, null);
    }

    ActionState(String buttonText, Role accessRole) {
        this.buttonText = buttonText;
        this.accessRole = accessRole;
    }

    @Override
    public Predicate<Long> hasAccess() {
        return null;
    }

    @Override
    public boolean hasAccess(User user) {
        return accessRole == null || user.getRoles().contains(accessRole);
    }
}

