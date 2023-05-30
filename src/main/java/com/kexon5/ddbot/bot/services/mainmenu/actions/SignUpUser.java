package com.kexon5.ddbot.bot.services.mainmenu.actions;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.kexon5.ddbot.bot.services.ActionElement;
import com.kexon5.ddbot.models.User;
import com.kexon5.ddbot.repositories.UserRepository;
import com.kexon5.ddbot.statemachine.DialogueFlow;
import com.kexon5.ddbot.utils.Utils;
import com.kexon5.ddbot.utils.markup.BoldString;
import lombok.Setter;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;

import static com.kexon5.ddbot.bot.services.ActionState.SIGN_UP_USER;
import static com.kexon5.ddbot.models.RegistrationConstants.bloodGroups;
import static com.kexon5.ddbot.models.RegistrationConstants.factors;
import static com.kexon5.ddbot.utils.Utils.*;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

public class SignUpUser extends ActionElement {

    private final UserRepository userRepository;

    public SignUpUser(UserRepository userRepository) {
        super(
                SIGN_UP_USER,
                userId -> false,
                SignUpSteps.values()
        );

        this.userRepository = userRepository;
        SignUpSteps.setUserRepository(userRepository);
    }

    @Override
    public DialogueFlow.DialogueFlowBuilder setAdditional(DialogueFlow.DialogueFlowBuilder builder) {
        return builder.onlyIf(update -> !userRepository.existsByUserId(getChatId(update)))
                      .onlyIf(update -> builder.getUserStateId(update) == -1);
    }

    public enum SignUpSteps implements ActionMessageState {
        GREETINGS {

            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, @Nonnull Document userDocument) {
                builder.replyMarkup(YES_NO.build());
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return "Привет, дорогой путник!\n" +
                        "Для моей корректной работы мне нужна некоторая информация о тебе:)\n" +
                        "Поделишься?";
            }

            @Override
            public String validate(long userId, String userText, Document document) {
                return userText.equals(YES) || userText.equals(NO)
                        ? userText
                        : null;
            }
        },
        NAME {
            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return (userDocument.getString(GREETINGS.name()).equals(NO)
                        ? "Так, а зачем ты пришёл ко мне, шутник?\nЛадно...\n\n"
                        : "Отлично!\n\n")
                        + new BoldString("Напиши свои фамилию, имя и, если есть/хочешь, отчество");
            }

            @Override
            public String validate(long userId, String userText, Document document) {
                String[] words = userText.split(" ");
                if (words.length != 2 && words.length != 3) return null;

                return Arrays.stream(words)
                                   .map(s -> s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase())
                                   .collect(Collectors.joining(" "));
            }
            
        },
        BIRTHDAY {
            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return "Дата рождения в формате " + new BoldString("ЧЧ-ММ-ГГГГ"); //todo change format
            }

            @Override
            public LocalDate validate(long userId, String userText, Document document) {
                return LocalDate.parse(userText, DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            }

        },
        SEX {
            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, @Nonnull Document userDocument) {
                builder.replyMarkup(W_M.build());
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return "Ваш пол?";
            }

            @Override
            public String validate(long userId, String userText, Document document) {
                return userText.equals(WOMAN) || userText.equals(MAN)
                        ? userText
                        : null;
            }
        },
        PHONE_NUMBER {
            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return "Номер телефона";
            }

            @Override
            public String validate(long userId, String userText, Document document) throws Exception {
                return phoneUtil.format(phoneUtil.parse(userText, "RU"), PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
            }
        },
        GROUP_NUMBER {
            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return "Номер группы";
            }

            @Override
            public String validate(long userId, String userText, Document document) {
                return userText.matches("[0-9]{7}/[0-9]{5}")
                        ? userText
                        : null;
            }
        },
        ASK_DETAILS {
            @Override
            public String jumpStateName() {
                return SUMMARIZE.name();
            }

            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, @Nonnull Document userDocument) {
                builder.replyMarkup(YES_NO.build());
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return "Знаете ли Вы информацию о своей группе крови, резус-факторе или Kell антигенах?";
            }

            @Override
            public String validate(long userId, String userText, Document document) {
                return userText.equals(YES)
                        ? userText
                        : null;
            }
        },
        BLOOD_GROUP {
            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, @Nonnull Document userDocument) {
                builder.replyMarkup(Utils.getReplyKeyboardMarkupBuilder(bloodGroups).build());
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return "Ваша группа крови";
            }

            @Override
            public String validate(long userId, String userText, Document document) {
                return bloodGroups.contains(userText)
                        ? userText
                        : null;
            }
        },
        RH_FACTOR {
            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, @Nonnull Document userDocument) {
                builder.replyMarkup(Utils.getReplyKeyboardMarkupBuilder(factors).build());
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return "Ваш резус-фактор";
            }

            @Override
            public String validate(long userId, String userText, Document document) {
                return factors.contains(userText)
                        ? userText
                        : null;
            }
        },
        KELL_FACTOR {
            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, @Nonnull Document userDocument) {
                builder.replyMarkup(Utils.getReplyKeyboardMarkupBuilder(factors).build());
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return "Ваш Kell";
            }

            @Override
            public String validate(long userId, String userText, Document document) {
                return factors.contains(userText)
                        ? userText
                        : null;
            }

        },
        SUMMARIZE {
            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, @Nonnull Document userDocument) {
                builder.replyMarkup(YES_NO.build());
            }

            private String getStringBloodGroup(String bloodGroup, String rhFactor) {
                return bloodGroup + "(" + rhFactor + ")";
            }

            @Override
            public String getAnswer(@Nullable String userText, @NotNull Document document) {
                String name = document.getString(NAME.name());
                LocalDate birthday = document.get(BIRTHDAY.name(), LocalDate.class);
                String sex = document.getString(SEX.name());
                String phoneNumber = document.getString(PHONE_NUMBER.name());
                String groupNumber = document.getString(GROUP_NUMBER.name());

                User.UserBuilder userBuilder = User.builder()
                                                   .name(name)
                                                   .birthday(birthday)
                                                   .sex(sex)
                                                   .phoneNumber(phoneNumber)
                                                   .groupNumber(groupNumber);

                StringBuilder sb = new StringBuilder("Суммарная информация о Вас:\n")
                        .append(new BoldString("\n\nВаше имя:\n")).append(name)
                        .append(new BoldString("\n\nДата рождения\n")).append(birthday)
                        .append(new BoldString("\n\nПол\n")).append(sex)
                        .append(new BoldString("\n\nНомер телефона:\n")).append(phoneNumber)
                        .append(new BoldString("\n\nНомер группы:\n")).append(groupNumber);

                if (document.containsKey(BLOOD_GROUP.name())) {
                    String bloodGroup = document.getString(BLOOD_GROUP.name());
                    String rhFactor = document.getString(RH_FACTOR.name());
                    String kellFactor = document.getString(KELL_FACTOR.name());

                    userBuilder.bloodGroup(bloodGroup)
                               .rhFactor(rhFactor)
                               .kellFactor(kellFactor);

                    sb.append(new BoldString("\n\nГруппа крова:\n")).append(getStringBloodGroup(bloodGroup, rhFactor))
                      .append(new BoldString("\n\nKell:\n")).append(kellFactor);
                }

                document.append("FORM", userBuilder);
                return sb.append(new BoldString("\n\nВсё верно?"))
                         .toString();
            }

            @Override
            public String validate(long userId, String userText, Document document) {
                return userText.equals(YES)
                        ? userText
                        : null;
            }
        },
        FINAL {
            @Override
            public void finalAction(long userId, @Nullable String userText, Document document) {
                User user = document.get("FORM", User.UserBuilder.class)
                                    .userId(userId)
                                    .build();

                userRepository.save(user);
            }

            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, @Nonnull Document userDocument) {
                builder.replyMarkup(new ReplyKeyboardRemove(true));
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return "Вы успешно зарегистрированы!";
            }
        };

        private static final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        @Setter
        private static UserRepository userRepository;

    }
}
