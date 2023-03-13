package com.kexon5.ddbot.actions.hospitals.edit;

import com.kexon5.ddbot.models.hospital.Hospital;
import com.kexon5.ddbot.models.hospital.HospitalBackup;
import com.kexon5.ddbot.repositories.HospitalBackupRepository;
import com.kexon5.ddbot.repositories.HospitalRepository;
import com.kexon5.ddbot.statemachine.BotState;
import com.kexon5.ddbot.statemachine.Eventable;
import com.kexon5.ddbot.utils.Utils;
import com.kexon5.ddbot.utils.markup.BoldString;
import com.kexon5.ddbot.utils.markup.MarkupList;
import lombok.Setter;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class EditHospital {
    public enum EditSteps implements BotState, Eventable {
        NAME {
            @Override
            public void init(long userId) {
                hospitals = EditSteps.hospitalRepository.findAll();
            }

            @Override
            public ReplyKeyboardMarkup getCustomOptionsToReplyMarkup() {
                return null;
            }

            @Override
            public String getMessageAnswer(String userText) {
                return "" + new MarkupList<>(hospitals.stream().map(Hospital::getName).toList()) +
                        new BoldString("\nВведите номер ОПК в списке, который хотите отредактировать");
            }

            public boolean validate(String text) {
                try {
                    index = Integer.parseInt(text) - 1;
                    return index < hospitals.size() && index >= 0;
                } catch (Exception e) {
                    return false;
                }
            }
        },
        ACCEPT {
            @Override
            public String getMessageAnswer(String userText) {
                return hospitals.get(index).getPlaceInfoText(true) + new BoldString("\nВы хотите отредактировать данные о следующем ОПК?");
            }

            @Override
            public ReplyKeyboardMarkup getCustomOptionsToReplyMarkup() {
                return null;
            }

        },
        REQUIRED_DOCUMENTS {
            @Override
            public String getMessageAnswer(String userText) {
                return String.format("Текущая информация по необходимым документам:\n\n%s", hospitals.get(index).getRequiredDocuments());
            }

            public boolean validate(String text) {
                try {
                    if (!Objects.equals(text, SKIP)) {
                        requiredDocuments = text.split("\n");
                    }
                    return true;
                } catch (Exception e) {
                    return false;//"Ты шо такое ввёл, что ошибка вылетела?\nПеределывай!";
                }
            }
        },
        HOW_TO_GET {
            @Override
            public String getMessageAnswer(String userText) {
                return String.format("Текущая информация по тому, как добраться:\n\n%s", hospitals.get(index).getHowToGet());
            }

            public boolean validate(String text) {
                try {
                    if (!Objects.equals(text, SKIP)) {
                        howToGet = text.split("\n");
                    }
                    return true;
                } catch (Exception e) {
                    return false;//"Ты шо такое ввёл, что ошибка вылетела?\nПеределывай!";
                }
            }
        },
        NOTES {
            @Override
            public String getMessageAnswer(String userText) {
                return String.format("Текущие примечания:\n\n%s", hospitals.get(index).getNotes());
            }

            public boolean validate(String text) {
                try {
                    if (!Objects.equals(text, SKIP)) {
                        notes = text.split("\n");
                    }
                    return true;
                } catch (Exception e) {
                    return false;//"Ты шо такое ввёл, что ошибка вылетела?\nПеределывай!";
                }
            }
        },
        CONTACT_INFO {
            @Override
            public String getMessageAnswer(String userText) {
                return String.format("Текущие контактные данные:\n\n%s", hospitals.get(index).getContactInfo());
            }

            public boolean validate(String text) {
                try {
                    if (!Objects.equals(text, SKIP)) {
                        contactInfo = text.split("\n");
                    }
                    return true;
                } catch (Exception e) {
                    return false;//"Ты шо такое ввёл, что ошибка вылетела?\nПеределывай!";
                }
            }
        },
        FINAL {
            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder) {
                builder.replyMarkup(new ReplyKeyboardRemove(true));
            }

            @Override
            public String getMessageAnswer(String userText) {
                return requiredDocuments == null && howToGet == null && notes == null && contactInfo == null
                        ? "Успешно ничего не сделано:)"
                        : "Успешно обновлено";
            }

            @Override
            public void action(long userId, String userText) {
                if (updateData()) {
                    hospitalBackupRepository.save(new HospitalBackup(hospitals.get(index)));

                    hospitals.get(index).updateData(requiredDocuments, howToGet, notes, contactInfo);
                    hospitalRepository.save(hospitals.get(index));
                }
            }

            public static boolean updateData() {
                return !(requiredDocuments == null && howToGet == null && notes == null && contactInfo == null);
            }
        };

        private static int index;
        @Setter
        private static List<Hospital> hospitals = new ArrayList<>();

        private static String[] requiredDocuments;
        private static String[] howToGet;
        private static String[] notes;
        private static String[] contactInfo;

        private static HospitalRepository hospitalRepository;
        private static HospitalBackupRepository hospitalBackupRepository;

        private static final String SKIP = "Пропустить";

        private static final ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder SKIP_MENU_BUILDER =
                Utils.getReplyKeyboardMarkupBuilder(List.of(new KeyboardButton(SKIP)));

        public ReplyKeyboardMarkup getCustomOptionsToReplyMarkup() {
            return SKIP_MENU_BUILDER.build();
        }

        @Override
        public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder) {
            Optional.ofNullable(getCustomOptionsToReplyMarkup()).ifPresent(builder::replyMarkup);
        }

    }

    public EditHospital(HospitalRepository hospitalRepository, HospitalBackupRepository hospitalBackupRepository) {
        EditSteps.hospitalRepository = hospitalRepository;
        EditSteps.hospitalBackupRepository = hospitalBackupRepository;
    }

}
