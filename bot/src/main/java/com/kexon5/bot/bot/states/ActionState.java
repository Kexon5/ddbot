package com.kexon5.bot.bot.states;


import com.kexon5.bot.statemachine.Buttonable;
import com.kexon5.common.models.Roles;
import com.kexon5.common.models.User;
import lombok.Getter;

import java.util.function.Function;

@Getter
public enum ActionState implements Buttonable {
    EDIT_HOSPITAL("✒️Отредактировать данные об ОПК", Roles.ADMIN),
    ADD_HOSPITAL("➕Добавить ОПК", Roles.ADMIN),
    CREATE_SCHEDULE("➕Создать расписание", Roles.MAIN_HEAD),
    READ_SCHEDULE("💫Загрузить данные из расписания", Roles.MAIN_HEAD),
    OPEN_REGISTRATION("🟢Открыть регистрацию", Roles.HEAD),
    CHECK_IN_USER("✒️Записаться на выезд"),
    CHECK_OUT_USER("❌Отменить запись"),
    GRANT_ROLES("🔝Дать права пользователю", Roles.ADMIN),
    SIGN_UP_USER("TEST"),

    BACK( "🔙Назад");

    @Getter
    private final String buttonText;
    private final Function<User, Boolean> accessPredicate;

    ActionState(String buttonText) {
        this.buttonText = buttonText;
        this.accessPredicate = user -> true;
    }

    ActionState(String buttonText, Roles role) {
        this.buttonText = buttonText;
        this.accessPredicate = user -> user.getRoles().contains(role);
    }

}

