package com.kexon5.ddbot.statemachine;

import com.google.common.collect.ImmutableList;
import com.kexon5.ddbot.models.hospital.Hospital;
import com.kexon5.ddbot.services.ScheduleService;
import com.kexon5.ddbot.utils.BotMessage;
import com.kexon5.ddbot.utils.Utils;
import com.kexon5.ddbot.utils.markup.BoldString;
import com.kexon5.ddbot.utils.markup.MarkupList;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.Pair;
import org.springframework.statemachine.config.builders.StateMachineTransitionConfigurer;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

import static com.kexon5.ddbot.statemachine.ActionStateHolder.ActionState.*;

public class ServiceStateHolder {
    @RequiredArgsConstructor
    public enum ServiceState implements Configurable, Buttonable {
        EDIT_HOSPITALS_MENU(EDIT_HOSPITALS_MENU_BUTTON, Collections.EMPTY_LIST, List.of(EDIT_HOSPITAL, ADD_HOSPITAL)) {
            @Override
            public String getMessageAnswer(String userText) {
                List<String> hospitalsList = new MarkupList<>(scheduleService.getAllHospitals().stream()
                        .map(Hospital::getName)
                        .toList());
                return String.valueOf(new BoldString("Текущий список больниц:\n\n")) + hospitalsList;
            }
        },
        HOSPITALS_MENU(HOSPITALS_MENU_BUTTON, List.of(EDIT_HOSPITALS_MENU), List.of(CREATE_SCHEDULE, READ_SCHEDULE)) {

            private static final MarkupList<String> actionDetails = new MarkupList<>(new ImmutableList.Builder<String>()
                    .add("Отредактировать информацию об ОПК", "Получить ссылку на таблицу с расписанием", "Импортировать записи из таблицы").build());

            @Override
            public String getMessageAnswer(String userText) {
                return "В данном разделе Вы можете:\n" + actionDetails;
            }
        },
        MAIN_MENU(MAIN_MENU_BUTTON, List.of(HOSPITALS_MENU), List.of(SIGNUP_USER, CHECKOUT_USER)) {
            @Override
            public String getMessageAnswer(String userText) {
                return "Добро пожаловать, путник!";
            }

            @Override
            public BotMessage getMessage(long userId, int msgId, String userText, boolean isMsg) {
                BotApiMethod<? extends Serializable> msg = isMsg
                        ? SendMessage.builder()
                                     .chatId(userId)
                                     .text(getMessageAnswer(userText))
                                     .replyMarkup(getFilteredMenu(userId))
                                     .build()
                        : EditMessageText.builder()
                                         .chatId(userId)
                                         .text(getMessageAnswer(userText))
                                         .replyMarkup(getFilteredMenu(userId))
                                         .messageId(msgId)
                                         .build();

                return new BotMessage(msg);
            }
        };

        @Getter
        private final String buttonText;
        private final List<ServiceState> servicesList;
        private final List<ActionStateHolder.ActionState> actionsList;

        @Getter
        private final List<Pair<Predicate<Long>, InlineKeyboardButton>> buttons = new ArrayList<>();
        private static ScheduleService scheduleService;

        @Override
        public void configureEvents(StateMachineTransitionConfigurer<BotState, Integer> transitions) throws Exception {
            int serviceListSize = servicesList.size();
            for (int i = 0; i < serviceListSize; i++) {
                ServiceState serviceState = servicesList.get(i);
                transitions
                        .withExternal()
                        .source(this).target(serviceState).event(i + 1)
                        .and()
                        .withExternal()
                        .source(serviceState).target(this).event(-1);

                buttons.add(getSecuredButton(i + 1, serviceState));
            }

            for (int i = 0; i < actionsList.size(); i++) {
                ActionStateHolder.ActionState actionState = actionsList.get(i);
                transitions.withExternal().source(this).target(actionState).event(serviceListSize + i + 1);
                buttons.add(getSecuredButton(serviceListSize + i + 1, actionState));
            }

            if (!this.equals(MAIN_MENU)) {
                buttons.add(getButton(-1, BACK_BUTTON));
                if (!MAIN_MENU.servicesList.contains(this)) {
                    transitions.withExternal().source(this).target(MAIN_MENU).event(-2);
                    buttons.add(getButton(-2, MAIN_MENU.buttonText));
                }
            }
        }


        public BotMessage getMessage(long userId, int msgId, String userText, boolean isMsg) {
            return new BotMessage(List.of(EditMessageText.builder()
                    .chatId(userId)
                    .text(getMessageAnswer(userText))
                    .replyMarkup(getFilteredMenu(userId))
                    .parseMode("Markdown")
                    .messageId(msgId)
                    .build()));
        }

        public InlineKeyboardMarkup getFilteredMenu(long userId) {
            return Utils.getMenu(buttons.stream()
                    .filter(b -> b.getFirst().test(userId))
                    .map(Pair::getSecond).toList());
        }

        private static Pair<Predicate<Long>, InlineKeyboardButton> getSecuredButton(int eventCode, Buttonable buttonable) {
            return Pair.of(buttonable.hasAccess(), Utils.getButton(eventCode, buttonable.getButtonText()));
        }

        private static Pair<Predicate<Long>, InlineKeyboardButton> getButton(int eventCode, String buttonText) {
            return Pair.of(userId -> true, Utils.getButton(eventCode, buttonText));
        }

    }

    public ServiceStateHolder(ScheduleService scheduleService) {
        ServiceState.scheduleService = scheduleService;
    }
}
