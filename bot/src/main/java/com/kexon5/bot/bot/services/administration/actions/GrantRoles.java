package com.kexon5.bot.bot.services.administration.actions;

import com.kexon5.bot.bot.elements.ActionElement;
import com.kexon5.bot.bot.states.ActionState;
import com.kexon5.bot.utils.Utils;
import com.kexon5.bot.utils.markup.BoldString;
import com.kexon5.bot.utils.markup.MarkupList;
import com.kexon5.common.models.Role;
import com.kexon5.common.models.User;
import com.kexon5.common.repositories.UserRepository;
import org.bson.Document;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.kexon5.bot.utils.Utils.YES;
import static com.kexon5.bot.utils.Utils.YES_NO;
import static com.kexon5.common.models.Role.rolesMap;

public class GrantRoles extends ActionElement {

    public GrantRoles(ActionState actionState, Predicate<Long> predicate, UserRepository userRepository) {
        super(actionState, predicate, GrantSteps.values());

        GrantSteps.userRepository = userRepository;
    }

    public enum GrantSteps implements ActionMessageState {
        USER_INPUT {
            @Override
            public void initAction(long userId, Document userDocument) {
                userDocument.append(CALL_USER, userRepository.findByUserId(userId));
            }

            @Override
            public String jumpStateName() {
                return CONFIRM_USER.name();
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document document) {
                return "Пожалуйста, введите фамилию пользователя";
            }

            @Override
            public List<User> validate(long userId, String userText, Document document) throws Exception {
                User callUser = document.get(CALL_USER, User.class);
                Set<Role> callUserRoles = callUser.getRoles();

                List<User> users = userRepository.findAllByNameContainsIgnoreCase(userText).stream()
                                                 .filter(user -> user.getRoles().size() < callUserRoles.size())
                                                 .filter(user -> user.getUserId() != callUser.getUserId())
                                                 .toList();

                // TODO: 01.06.2023 добавить логику, если никто не найден
                if (users.size() != 1) {
                    return users;
                }

                document.append(USER_NUMBER.name(), users.get(0));
                return null;
            }
        },
        USER_NUMBER {

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document document) {
                List<String> users = document.getList(USER_INPUT.name(), User.class).stream()
                                             .map(User::toShortString)
                                             .collect(Collectors.toCollection(MarkupList::new));

                return users.toString()
                        + new BoldString("\n\nВведите номер пользователя, которому хотите дать права");
            }

            @Override
            public User validate(long userId, String userText, Document document) {
                List<User> users = document.getList(USER_INPUT.name(), User.class);
                int index = Integer.parseInt(userText) - 1;

                return index < users.size() && index >= 0
                        ? users.get(index)
                        : null;
            }
        },
        CONFIRM_USER {
            @Override
            public String jumpStateName() {
                return GRANT_RESULT.name();
            }

            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, Document document) {
                builder.replyMarkup(YES_NO.build());
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document document) {
                return new BoldString("Вы хотите дать права следующему пользователю?\n\n")
                        + document.get(USER_NUMBER.name(), User.class).toShortString();
            }

            @Override
            public String validate(long userId, String userText, Document document) throws Exception {
                return userText.equals(YES)
                        ? userText
                        : null;
            }
        },
        ROLE_LEVEL {
            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, Document document) {
                Set<Role> callUserRoles = document.get(CALL_USER, User.class).getRoles();

                List<Role> possibleGrantRoles = callUserRoles.stream()
                                                             .sorted(Comparator.comparingInt(Enum::ordinal))
                                                             .toList();

                document.append(GRANT_ROLES, possibleGrantRoles);
                builder.replyMarkup(Utils.getReplyKeyboardMarkupBuilder(possibleGrantRoles).build());
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document document) {
                return "Выберите уровень прав";
            }

            @Override
            public Role validate(long userId, String userText, Document document) {
                Role role = Role.valueOf(userText);
                return document.getList(GRANT_ROLES, Role.class).contains(role)
                        ? role
                        : null;
            }
        },
        GRANT_RESULT {
            @Override
            public void finalAction(long userId, @Nullable String userText, Document document) {
                if (document.containsKey(CONFIRM_USER.name())) {
                    User user = document.get(USER_NUMBER.name(), User.class);
                    Role roleGroup = document.get(ROLE_LEVEL.name(), Role.class);

                    user.setRoles(rolesMap.get(roleGroup));
                    userRepository.save(user);
                }
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document document) {
                return document.containsKey(CONFIRM_USER.name())
                        ? "Права успешно предоставлены"
                        : "Ничего не сделано";
            }
        };


        private static final String GRANT_ROLES = "GRANT_ROLES";
        private static final String CALL_USER = "CALL_USER";

        private static UserRepository userRepository;
    }
}
