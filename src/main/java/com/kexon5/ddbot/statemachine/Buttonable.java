package com.kexon5.ddbot.statemachine;

import java.util.function.Predicate;

public interface Buttonable {
    //    ACTIONS
    String EDIT_HOSPITAL_BUTTON = "✒️ Отредактировать данные об ОПК";
    String ADD_HOSPITAL_BUTTON = "➕ Добавить ОПК";
    String CREATE_SCHEDULE_BUTTON = "➕ Создать расписание";
    String READ_SCHEDULE_BUTTON = "💫 Загрузить данные из расписания";
    String SIGNUP_USER_BUTTON = "✒️ Записаться на выезд";
    String CHECKOUT_USER_BUTTON = "❌Отменить запись";

    // SERVICES
    String EDIT_HOSPITALS_MENU_BUTTON = "✍🏻 Редактирование ОПК";
    String HOSPITALS_MENU_BUTTON = "🏥 Настройка ОПК";
    String MAIN_MENU_BUTTON = "🏡 Вернуться в главное меню";

    // UTILS
    String BACK_BUTTON = "🔙 Назад";

    String getButtonText();

    default Predicate<Long> hasAccess() {
        return userId -> true;
    }

}
