package com.kexon5.ddbot.bot.services.administration.actions;

import com.kexon5.ddbot.bot.elements.ActionElement;
import com.kexon5.ddbot.bot.states.ActionState;
import com.kexon5.ddbot.models.Roles;
import com.kexon5.ddbot.models.User;
import com.kexon5.ddbot.repositories.UserRepository;
import com.kexon5.ddbot.utils.Utils;
import com.kexon5.ddbot.utils.markup.BoldString;
import com.kexon5.ddbot.utils.markup.MarkupList;
import org.bson.Document;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.kexon5.ddbot.models.Roles.rolesMap;
import static com.kexon5.ddbot.utils.Utils.YES;
import static com.kexon5.ddbot.utils.Utils.YES_NO;

public class GrantRoles extends ActionElement {

    public GrantRoles(ActionState actionState, Predicate<Long> predicate, UserRepository userRepository) {
        super(actionState, predicate, GrantSteps.values());

        GrantSteps.userRepository = userRepository;
    }

    public enum GrantSteps implements ActionMessageState {
        STEP1 {
            @Override
            public void initAction(long userId, Document userDocument) {
                userDocument.append("CALL_USER", userRepository.findByUserId(userId));
            }

            @Override
            public String jumpStateName() {
                return STEP3.name();
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document document) {
                return "Пожалуйста, введите фамилию пользователя";
            }

            @Override
            public List<User> validate(long userId, String userText, Document document) throws Exception {
                User callUser = document.get("CALL_USER", User.class);
                Set<Roles> callUserRoles = callUser.getRoles();

                List<User> users = userRepository.findAllByNameContainsIgnoreCase(userText).stream()
                                                 .filter(user -> user.getRoles().size() < callUserRoles.size())
                                                 .filter(user -> user.getUserId() != callUser.getUserId())
                                                 .toList();

                // TODO: 01.06.2023 добавить логику, если никто не найден
                if (users.size() != 1) {
                    return users;
                }

                document.append(STEP2.name(), users.get(0));
                return null;
            }
        },
        STEP2 {

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document document) {
                List<String> users = document.getList(STEP1.name(), User.class).stream()
                                             .map(User::toShortString)
                                             .collect(Collectors.toCollection(MarkupList::new));

                return users.toString()
                        + new BoldString("\n\nВведите номер пользователя, которому хотите дать права");
            }

            @Override
            public User validate(long userId, String userText, Document document) {
                List<User> users = document.getList(STEP1.name(), User.class);
                int index = Integer.parseInt(userText) - 1;

                return index < users.size() && index >= 0
                        ? users.get(index)
                        : null;
            }
        },
        STEP3 {
            @Override
            public String jumpStateName() {
                return STEP5.name();
            }

            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, Document document) {
                builder.replyMarkup(YES_NO.build());
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document document) {
                return new BoldString("Вы хотите дать права следующему пользователю?\n\n")
                        + document.get(STEP2.name(), User.class).toShortString();
            }

            @Override
            public String validate(long userId, String userText, Document document) throws Exception {
                return userText.equals(YES)
                        ? userText
                        : null;
            }
        },
        STEP4 {
            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, Document document) {
                Set<Roles> callUserRoles = document.get("CALL_USER", User.class).getRoles();

                List<Roles> possibleGrantRoles = callUserRoles.stream()
                                                              .sorted(Comparator.comparingInt(Enum::ordinal))
                                                              .toList();

                document.append("GRANT_ROLES", possibleGrantRoles);
                builder.replyMarkup(Utils.getReplyKeyboardMarkupBuilder(possibleGrantRoles).build());
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document document) {
                return "Выберите уровень прав";
            }

            @Override
            public Roles validate(long userId, String userText, Document document) {
                Roles role = Roles.valueOf(userText);
                return document.getList("GRANT_ROLES", Roles.class).contains(role)
                        ? role
                        : null;
            }
        },
        STEP5 {
            @Override
            public void finalAction(long userId, @Nullable String userText, Document document) {
                if (document.containsKey(STEP3.name())) {
                    User user = document.get(STEP2.name(), User.class);
                    Roles rolesGroup = document.get(STEP4.name(), Roles.class);

                    user.setRoles(rolesMap.get(rolesGroup));
                    userRepository.save(user);
                }
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document document) {
                return document.containsKey(STEP3.name())
                        ? "Права успешно предоставлены"
                        : "Ничего не сделано";
            }
        };

        private static UserRepository userRepository;
    }
}
