package com.kexon5.bot.bot.services.edithospital.actions;

import com.kexon5.bot.bot.elements.ActionElement;
import com.kexon5.bot.bot.states.ActionState;
import com.kexon5.bot.models.hospital.Hospital;
import com.kexon5.bot.models.hospital.HospitalBackup;
import com.kexon5.bot.repositories.HospitalBackupRepository;
import com.kexon5.bot.repositories.HospitalRepository;
import com.kexon5.bot.utils.Utils;
import com.kexon5.bot.utils.markup.BoldString;
import com.kexon5.bot.utils.markup.MarkupList;
import org.bson.Document;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

import static com.kexon5.bot.utils.Utils.YES;
import static com.kexon5.bot.utils.Utils.YES_NO;

public class EditHospital extends ActionElement {

    public EditHospital(ActionState actionState,
                        HospitalRepository hospitalRepository,
                        HospitalBackupRepository hospitalBackupRepository) {
        super(actionState, EditSteps.values());

        EditSteps.hospitalRepository = hospitalRepository;
        EditSteps.hospitalBackupRepository = hospitalBackupRepository;
    }

    public enum EditSteps implements ActionMessageState {
        NAME {
            @Override
            public void initAction(long userId, Document userDocument) {
                userDocument.append(HOSPITALS, EditSteps.hospitalRepository.findAll());
            }

            @Override
            public ReplyKeyboardMarkup getCustomOptionsToReplyMarkup() {
                return null;
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return "" + new MarkupList<>(EditSteps.getHospitals(userDocument).stream()
                                                      .map(Hospital::getName)
                                                      .toList()) +
                        new BoldString("\nВведите номер ОПК в списке, который хотите отредактировать");
            }

            @Override
            public Integer validate(long userId, String userText, Document document) {
                int hospitalsSize = EditSteps.getHospitals(document).size();
                int index = Integer.parseInt(userText) - 1;

                return index < hospitalsSize && index >= 0
                        ? index
                        : null;
            }
        },
        ACCEPT {
            @Override
            public ReplyKeyboardMarkup getCustomOptionsToReplyMarkup() {
                return YES_NO.build();
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return EditSteps.getHospital(userDocument).getPlaceInfoText(true)
                        + new BoldString("\nВы хотите отредактировать данные о следующем ОПК?");
            }

            @Override
            public Boolean validate(long userId, String userText, Document document) {
                return userText.equals(YES)
                        ? true
                        : null;
            }

            @Override
            public String errorText() {
                return "Йоу, чел, да или нет... В чём проблема?";
            }
        },
        REQUIRED_DOCUMENTS {
            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return String.format(
                        "Текущая информация по необходимым документам:\n\n%s",
                        EditSteps.getHospital(userDocument).getRequiredDocuments()
                );
            }

            @Override
            public String[] validate(long userId, String userText, Document document) {
                return userText.split("\n");
            }

        },
        HOW_TO_GET {
            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return String.format(
                        "Текущая информация по тому, как добраться:\n\n%s",
                        EditSteps.getHospital(userDocument).getHowToGet()
                );
            }

            @Override
            public String[] validate(long userId, String userText, Document document) {
                return userText.split("\n");
            }
        },
        NOTES {
            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return String.format("Текущие примечания:\n\n%s", EditSteps.getHospital(userDocument).getNotes());
            }

            @Override
            public String[] validate(long userId, String userText, Document document) {
                return userText.split("\n");
            }
        },
        CONTACT_INFO {
            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return String.format("Текущие контактные данные:\n\n%s", EditSteps.getHospital(userDocument).getContactInfo());
            }

            @Override
            public String[] validate(long userId, String userText, Document document) {
                return userText.split("\n");
            }
        },
        FINAL {
            @Override
            public void finalAction(long userId, @Nullable String userText, Document userDocument) {
                if (updateData(userDocument)) {
                    hospitalBackupRepository.save(new HospitalBackup(EditSteps.getHospital(userDocument)));

                    EditSteps.getHospital(userDocument).updateData(userDocument);
                    hospitalRepository.save(EditSteps.getHospital(userDocument));
                }
            }

            @Override
            public void setOptionsToBuilder(long userId, SendMessage.SendMessageBuilder builder) {
                builder.replyMarkup(new ReplyKeyboardRemove(true));
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return updateData(userDocument)
                        ? "Успешно обновлено"
                        : "Успешно ничего не сделано:)";
            }
        };

        private static HospitalRepository hospitalRepository;
        private static HospitalBackupRepository hospitalBackupRepository;

        private static final String SKIP = "Пропустить";
        private static final String HOSPITALS = "HOSPITALS";

        private static final ReplyKeyboardMarkup.ReplyKeyboardMarkupBuilder SKIP_MENU_BUILDER =
                Utils.getReplyKeyboardMarkupBuilder(List.of(SKIP));


        private static List<Hospital> getHospitals(Document userDocument) {
            return userDocument.getList(HOSPITALS, Hospital.class);
        }

        private static Hospital getHospital(Document userDocument) {
            return userDocument.getList(HOSPITALS, Hospital.class)
                               .get(userDocument.getInteger(NAME.name()));
        }

        private static boolean updateData(Document userDocument, EditSteps step) {
            String[] lines = userDocument.get(step.name(), String[].class);
            return !(lines.length == 1 && lines[0].equals(SKIP));
        }

        public static boolean updateData(Document userDocument) {
            return updateData(userDocument, REQUIRED_DOCUMENTS) && updateData(userDocument, HOW_TO_GET)
                    && updateData(userDocument, NOTES) && updateData(userDocument, CONTACT_INFO);
        }

        public ReplyKeyboardMarkup getCustomOptionsToReplyMarkup() {
            return SKIP_MENU_BUILDER.build();
        }

        @Override
        public void setOptionsToBuilder(long userId, SendMessage.SendMessageBuilder builder) {
            Optional.ofNullable(getCustomOptionsToReplyMarkup()).ifPresent(builder::replyMarkup);
        }

    }

}
