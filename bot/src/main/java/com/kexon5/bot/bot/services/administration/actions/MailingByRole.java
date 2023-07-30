package com.kexon5.bot.bot.services.administration.actions;

import com.kexon5.bot.bot.elements.ActionElement;
import com.kexon5.bot.bot.states.ActionState;
import com.kexon5.bot.services.MailingService;
import com.kexon5.bot.utils.Utils;
import com.kexon5.bot.utils.markup.BoldString;
import com.kexon5.common.models.Role;
import com.kexon5.common.models.User;
import com.kexon5.common.repositories.UserRepository;
import org.bson.Document;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

import static com.kexon5.bot.utils.Utils.*;

public class MailingByRole extends ActionElement {

    public MailingByRole(ActionState actionState,
                         UserRepository userRepository,
                         MailingService mailingService) {
        super(actionState, MailingSteps.values());

        MailingSteps.userRepository = userRepository;
        MailingSteps.mailingService = mailingService;
    }

    public enum MailingSteps implements ActionMessageState {
        MESSAGE_INPUT {

            @Override
            public void initAction(long userId, Document userDocument) {
                userDocument.append(CALL_USER, userRepository.findByUserId(userId));
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document document) {
                return "Пожалуйста, введите сообщение, которое хотите разослать";
            }

            @Override
            public String validate(long userId, String userText, Document document) {
                return userText;
            }

        },
        CHOOSE_ROLE {

            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, Document document) {
                Set<Role> callUserRoles = document.get(CALL_USER, User.class).getRoles();

                List<Role> possibleMailingRoles = callUserRoles.stream()
                                                             .sorted(Comparator.comparingInt(Enum::ordinal))
                                                             .toList();

                document.append(MAILING_ROLES, possibleMailingRoles);
                builder.replyMarkup(Utils.getReplyKeyboardMarkupBuilder(possibleMailingRoles).build());
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document document) {
                return "Выберите роль получателей";
            }

            @Override
            public Role validate(long userId, String userText, Document document) {
                Role role = Role.valueOf(userText);
                return document.getList(MAILING_ROLES, Role.class).contains(role)
                        ? role
                        : null;
            }
        },
        SEND_TIME {

            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, Document document) {
                builder.replyMarkup(Utils.getReplyKeyboardMarkupBuilder(List.of(NOW, ONE_HOUR)).build());
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document document) {
                return "Через сколько отправить данное сообщение?";
            }

            @Override
            public String validate(long userId, String userText, Document document) throws Exception {
                return userText.equals(NOW) || userText.equals(ONE_HOUR)
                        ? userText
                        : null;
            }
        },
        CHECK_MAILING {
            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, Document document) {
                builder.replyMarkup(YES_NO.build());
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document document) {
                return "Пожалуйста, проверьте данные рассылки" +
                        new BoldString("\n\nОтправляемое сообщение\n") + document.getString(MESSAGE_INPUT.name()) +
                        new BoldString("\n\nРоль получателей\n") + document.get(CHOOSE_ROLE.name()).toString() +
                        new BoldString("\n\nКогда отправить\n") + document.getString(SEND_TIME.name()) +
                        new BoldString("\n\nВсё верно?");
            }

            @Override
            public String validate(long userId, String userText, Document document) {
                return userText.equals(YES) || userText.equals(NO)
                        ? userText
                        : null;
            }
        },
        MAILING_RESULT {

            private void addMessage(String msg, User user, long shift) {
                mailingService.addAfterDateMsg(msg + new BoldString("\n\nАвтор: @" + user.getUsername()), user.getUserId(), LocalDateTime.now(), shift, ChronoUnit.HOURS);
            }

            @Override
            public void finalAction(long userId, @Nullable String userText, Document document) {
                if (document.getString(CHECK_MAILING.name()).equals(YES)) {
                    String msg = document.getString(MESSAGE_INPUT.name());
                    Role role = document.get(CHOOSE_ROLE.name(), Role.class);
                    long shift = document.getString(SEND_TIME.name()).equals(NOW) ? 0 : 1;

                    userRepository.findAll().stream()
                            .filter(user -> user.getRoles().contains(role))
                            .forEach(user -> addMessage(msg, user, shift));
                }
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document document) {
                return document.getString(CHECK_MAILING.name()).equals(YES)
                        ? "Успешно выполнено"
                        : "Ничего не сделано";
            }
        };

        private static final String MAILING_ROLES = "MAILING_ROLES";
        private static final String CALL_USER = "CALL_USER";


        private static final String NOW = "Сейчас";
        private static final String ONE_HOUR =  "Через час";

        private static UserRepository userRepository;
        private static MailingService mailingService;
    }
}
