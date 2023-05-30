package com.kexon5.ddbot.bot.services;

import com.kexon5.ddbot.statemachine.Buttonable;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ActionState implements Buttonable {
    EDIT_HOSPITAL("✒️ Отредактировать данные об ОПК"),
    ADD_HOSPITAL("➕ Добавить ОПК"),
    CREATE_SCHEDULE("➕ Создать расписание"),
    READ_SCHEDULE("💫 Загрузить данные из расписания"),
    OPEN_REGISTRATION("🟢 Открыть регистрацию"),
    CHECK_IN_USER("✒️ Записаться на выезд"),
    CHECK_OUT_USER("❌ Отменить запись"),
    SIGN_UP_USER("TEST"),
    BACK( "🔙 Назад");

    @Getter
    private final String buttonText;

}

