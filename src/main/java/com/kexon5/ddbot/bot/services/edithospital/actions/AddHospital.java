package com.kexon5.ddbot.bot.services.edithospital.actions;

import com.kexon5.ddbot.bot.services.ActionElement;
import com.kexon5.ddbot.models.hospital.Hospital;
import com.kexon5.ddbot.repositories.HospitalRepository;
import com.kexon5.ddbot.utils.markup.MarkupList;
import org.bson.Document;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.kexon5.ddbot.bot.services.ActionState.ADD_HOSPITAL;

public class AddHospital extends ActionElement {

    public AddHospital(HospitalRepository hospitalRepository) {
        super(ADD_HOSPITAL, AddSteps.values());

        AddSteps.hospitalRepository = hospitalRepository;
    }

    public enum AddSteps implements ActionMessageState {
        PLACE_NAME {
            @Override
            public String getAnswer(long userId, @Nullable String userText) {
                return "Введите название ОПК";
            }

            @Override
            public String validate(long userId, String userText, Document document) {
                return userText;
            }
        },
        SIMPLE_NAME {
            @Override
            public String getAnswer(long userId, @Nullable String userText) {
                return "Введите разговорное название ОПК";
            }

            @Override
            public String validate(long userId, String userText, Document document) {
                return userText;
            }
        },
        COORDS {
            @Override
            public String getAnswer(long userId, @Nullable String userText) {
                return "Введите десятичные координаты этой ОПК";
            }

            @Override
            public List<Double> validate(long userId, String userText, Document document) {
                String[] coords = userText.split(" ");
                if (coords.length != 2) {
                    return null;
                }
                return List.of(Double.parseDouble(coords[0]), Double.parseDouble(coords[1]));
            }

            @Override
            public String errorText() {
                return "Координаты должны быть представлены двумя числами, разделёнными пробелом";
            }
        },
        HOW_TO_GET {
            @Override
            public String getAnswer(long userId, @Nullable String userText) {
                return "Инструкция как добраться (Пожалуйста, одним сообщением)";
            }

            @Override
            public MarkupList<String> validate(long userId, String userText, Document document) {
                return Arrays.stream(userText.split("\n"))
                             .collect(Collectors.toCollection(MarkupList::new));
            }
        },
        REQUIRED_DOCUMENTS {
            @Override
            public String getAnswer(long userId, @Nullable String userText) {
                return "Необходимые документы (Пожалуйста, одним сообщением)";
            }

            @Override
            public MarkupList<String> validate(long userId, String userText, Document document) {
                return Arrays.stream(userText.split("\n"))
                             .collect(Collectors.toCollection(MarkupList::new));
            }
        },
        NOTES {
            @Override
            public String getAnswer(long userId, @Nullable String userText) {
                return "Какие-то примечания?";
            }

            @Override
            public MarkupList<String> validate(long userId, String userText, Document document) {
                return Arrays.stream(userText.split("\n"))
                             .collect(Collectors.toCollection(MarkupList::new));
            }

        },
        CONTACT_INFO {
            @Override
            public String getAnswer(long userId, @Nullable String userText) {
                return "Информация о заведующих/ответственных";
            }

            @Override
            public MarkupList<String> validate(long userId, String userText, Document document) {
                return Arrays.stream(userText.split("\n"))
                             .collect(Collectors.toCollection(MarkupList::new));
            }
        },
        FINAL {
            @Override
            public String getAnswer(long userId, @Nullable String userText) {
                return "Успешно добавлено";
            }

            private static Hospital createFromDocument(Document hospitalDocument) {
                return Hospital.builder()
                               .name(hospitalDocument.getString(PLACE_NAME.name()))
                               .simpleName(hospitalDocument.getString(SIMPLE_NAME.name()))
                               .coords(hospitalDocument.getList(COORDS.name(), Double.class))
                               .howToGet(new MarkupList<>(hospitalDocument.getList(HOW_TO_GET.name(), String.class)))
                               .requiredDocuments(new MarkupList<>(hospitalDocument.getList(REQUIRED_DOCUMENTS.name(), String.class)))
                               .notes(new MarkupList<>(hospitalDocument.getList(NOTES.name(), String.class)))
                               .contactInfo(new MarkupList<>(hospitalDocument.getList(CONTACT_INFO.name(), String.class)))
                               .build();
            }

            @Override
            public void finalAction(long userId, @Nullable String userText, Document userDocument) {
                hospitalRepository.save(createFromDocument(userDocument));
            }

        };

        private static HospitalRepository hospitalRepository;

    }
}
