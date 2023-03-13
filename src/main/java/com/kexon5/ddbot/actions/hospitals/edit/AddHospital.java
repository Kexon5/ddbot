package com.kexon5.ddbot.actions.hospitals.edit;

import com.kexon5.ddbot.models.hospital.Hospital;
import com.kexon5.ddbot.repositories.HospitalRepository;
import com.kexon5.ddbot.statemachine.BotState;
import com.kexon5.ddbot.statemachine.Eventable;
import com.kexon5.ddbot.utils.markup.MarkupList;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AddHospital {

    public enum AddSteps implements BotState, Eventable {
        PLACE_NAME {
            @Override
            public String getMessageAnswer(String userText) {
                return "Введите название ОПК";
            }

            public boolean validate(String text) {
                builder.name(text);
                return true;
            }
        },
        COORDS {
            @Override
            public String getMessageAnswer(String userText) {
                return "Введите десятичные координаты этой ОПК";
            }

            public boolean validate(String text) {
                String[] coords = text.split(" ");
                if (coords.length != 2) {
                    return false;
                }
                builder.coords(List.of(Double.parseDouble(coords[0]), Double.parseDouble(coords[1])));//todo
                return true;
            }
        },
        HOW_TO_GET {
            @Override
            public String getMessageAnswer(String userText) {
                return "Инструкция как добраться (Пожалуйста, одним сообщением)";
            }

            public boolean validate(String text) {
                builder.howToGet(Arrays.stream(text.split("\n")).collect(Collectors.toCollection(MarkupList::new)));
                return true;
            }
        },
        REQUIRED_DOCUMENTS {

            @Override
            public String getMessageAnswer(String userText) {
                return "Необходимые документы (Пожалуйста, одним сообщением)";
            }

            public boolean validate(String text) {
                builder.requiredDocuments(Arrays.stream(text.split("\n")).collect(Collectors.toCollection(MarkupList::new)));
                return true;
            }
        },
        NOTES {

            @Override
            public String getMessageAnswer(String userText) {
                return "Какие-то примечания?";
            }

            public boolean validate(String text) {
                builder.requiredDocuments(Arrays.stream(text.split("\n")).collect(Collectors.toCollection(MarkupList::new)));
                return true;
            }
        },
        CONTACT_INFO {

            @Override
            public String getMessageAnswer(String userText) {
                return "Информация о заведующих/ответственных";
            }

            public boolean validate(String text) {
                builder.contactInfo(Arrays.stream(text.split("\n")).collect(Collectors.toCollection(MarkupList::new)));
                return true;
            }
        },
        FINAL {

            @Override
            public String getMessageAnswer(String userText) {
                return "Успешно добавлено";
            }

            @Override
            public void action(long userId, String userText) {
                hospitalRepository.save(builder.build());
            }

        };

        private static final Hospital.HospitalBuilder builder = Hospital.builder();

        private static HospitalRepository hospitalRepository;

    }

    public AddHospital(HospitalRepository hospitalRepository) {
        AddSteps.hospitalRepository = hospitalRepository;
    }

}
