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
import java.util.List;
import java.util.stream.Collectors;

import static com.kexon5.ddbot.bot.services.ActionState.SIGN_UP_USER;
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
            public Boolean validate(long userId, String userText, Document document) {
                return userText.equals(WOMAN) || userText.equals(MAN)
                        ? userText.equals(MAN)
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
                builder.replyMarkup(Utils.getReplyKeyboardMarkupBuilder(List.of(1, 2, 3, 4)).build());
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return "Ваша группа крови";
            }

            @Override
            public Integer validate(long userId, String userText, Document document) {
                int group = Integer.parseInt(userText);
                return group >= 1 && group <= 4
                        ? group
                        : null;
            }
        },
        RH_FACTOR {
            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, @Nonnull Document userDocument) {
                builder.replyMarkup(Utils.getReplyKeyboardMarkupBuilder(List.of("+", "-")).build());
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return "Ваш резус-фактор";
            }

            @Override
            public String validate(long userId, String userText, Document document) {
                return userText.equals("+") || userText.equals("-")
                        ? userText
                        : null;
            }
        },
        KELL_FACTOR {
            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, @Nonnull Document userDocument) {
                builder.replyMarkup(Utils.getReplyKeyboardMarkupBuilder(List.of("+", "-")).build());
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return "Ваш Kell";
            }

            @Override
            public String validate(long userId, String userText, Document document) {
                return userText.equals("+") || userText.equals("-")
                        ? userText
                        : null;
            }

        },
        SUMMARIZE {
            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, @Nonnull Document userDocument) {
                builder.replyMarkup(YES_NO.build());
            }

            private String getStringBloodGroup(int bloodGroup, boolean rhFactor) {
                String rhFactorStr = rhFactor ? "(+)" : "(-)";
                switch (bloodGroup) {
                    case 1 -> {
                        return "I" + rhFactorStr;
                    }
                    case 2 -> {
                        return "II" + rhFactorStr;
                    }
                    case 3 -> {
                        return "III" + rhFactorStr;
                    }
                    case 4 -> {
                        return "IV" + rhFactorStr;
                    }
                    default -> {
                        return "Incorrect blood group";
                    }
                }
            }

            @Override
            public String getAnswer(@Nullable String userText, @NotNull Document document) {
                String name = document.getString(NAME.name());
                LocalDate birthday = document.get(BIRTHDAY.name(), LocalDate.class);
                boolean isMan = document.getBoolean(SEX.name());
                String phoneNumber = document.getString(PHONE_NUMBER.name());
                String groupNumber = document.getString(GROUP_NUMBER.name());

                User.UserBuilder userBuilder = User.builder()
                                                   .name(name)
                                                   .birthday(birthday)
                                                   .isMan(isMan)
                                                   .phoneNumber(phoneNumber)
                                                   .groupNumber(groupNumber);

                StringBuilder sb = new StringBuilder("Суммарная информация о Вас:\n")
                        .append(new BoldString("\n\nВаше имя:\n")).append(name)
                        .append(new BoldString("\n\nДата рождения\n")).append(birthday)
                        .append(new BoldString("\n\nПол\n")).append(isMan ? "Мужчина" : "Женщина")
                        .append(new BoldString("\n\nНомер телефона:\n")).append(phoneNumber)
                        .append(new BoldString("\n\nНомер группы:\n")).append(groupNumber);

                if (document.containsKey(BLOOD_GROUP.name())) {
                    int bloodGroup = document.getInteger(BLOOD_GROUP.name());
                    boolean rhFactor = document.getBoolean(RH_FACTOR.name());
                    boolean kellFactor = document.getBoolean(document.getBoolean(KELL_FACTOR.name()));

                    userBuilder.bloodGroup(bloodGroup)
                               .isRhPositive(rhFactor)
                               .isKellPositive(kellFactor);

                    sb.append(new BoldString("\n\nГруппа крова:\n")).append(getStringBloodGroup(bloodGroup, rhFactor))
                      .append(new BoldString("\n\nKell:\n")).append(kellFactor ? "Положительный" : "Отрицательный");
                }

                document.append(name(), userBuilder);
                return sb.toString();
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
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument) {
                return "Вы успешно зарегистрированы!";
            }

            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, @Nonnull Document userDocument) {
                builder.replyMarkup(new ReplyKeyboardRemove(true));
            }

            @Override
            public void finalAction(long userId, @Nullable String userText, Document document) {
                User user = document.get(SUMMARIZE.name(), User.UserBuilder.class)
                                    .userId(userId)
                                    .build();

                userRepository.save(user);
            }
        };

        private static final PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        @Setter
        private static UserRepository userRepository;

    }
}
