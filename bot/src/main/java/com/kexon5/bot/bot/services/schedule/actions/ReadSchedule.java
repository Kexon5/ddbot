package com.kexon5.bot.bot.services.schedule.actions;

import com.kexon5.bot.bot.elements.ActionElement;
import com.kexon5.bot.bot.states.ActionState;
import com.kexon5.bot.models.TableOption;
import com.kexon5.bot.models.hospital.HospitalRecord;
import com.kexon5.bot.services.RepositoryService;
import com.kexon5.bot.utils.markup.BoldString;
import com.kexon5.bot.utils.markup.MarkupList;
import org.bson.Document;
import org.jetbrains.annotations.NotNull;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.kexon5.bot.utils.Utils.*;

public class ReadSchedule extends ActionElement {

    public ReadSchedule(ActionState actionState,
                        Predicate<Long> predicate,
                        RepositoryService repositoryService) {
        super(actionState, predicate, ReadSteps.values());

        ReadSteps.repositoryService = repositoryService;
    }

    public enum ReadSteps implements ActionMessageState {
        SELECT_TABLE {

            @Override
            public String getAnswer(@Nullable String userText, @NotNull Document document) {
                return "Из какой таблицы Вы хотите загрузить расписание?";
            }

            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, Document document) {
                builder.replyMarkup(repositoryService.getTablesMarkup());
            }

            @Override
            public TableOption validate(long userId, String userText, Document document)  {
                return repositoryService.isTableOption(userText);
            }
        },
        READ() {

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document userDocument)  {
                TableOption option = userDocument.get(SELECT_TABLE.name(), TableOption.class);

                Map<TableOption, List<HospitalRecord>> tableData = repositoryService.readTable(option);

                StringBuilder sb = new StringBuilder().append("Считаны следующие записи:\n");

                tableData.forEach((opt, records) -> {
                    sb.append(new BoldString("\n\n" + opt.getText() + "\n\n"));

                    if (records.isEmpty()) {
                        sb.append(new BoldString("\nОшибка в схеме таблицы или записи не найдены\n"));
                        return;
                    }

                    fillStringBuilder(sb, records);
                });

                List<HospitalRecord> existRecords = repositoryService.findAllHospitalRecordsByHash(
                        tableData.values().stream()
                                 .flatMap(Collection::stream)
                                 .map(HospitalRecord::getRecordHash)
                                 .collect(Collectors.toSet())
                );

                if (!existRecords.isEmpty()) {
                    sb.append("\nПри добавлении сотрутся данные об этих записях:\n");

                    fillStringBuilder(sb, existRecords);

                    MarkupList<String> users = existRecords.stream()
                                                      .map(HospitalRecord::getUsers)
                                                      .flatMap(t -> repositoryService.findAllById(t).stream())
                                                      .map(user -> user.getName() + ", " + user.getPhoneNumber())
                                                      .collect(Collectors.toCollection(MarkupList::new));

                    if (!users.isEmpty()) {
                        sb.append("\nТак же будет необходимо перезаписать следующих доноров: \n\n")
                          .append(users);
                    }

                    userDocument.append("REMOVE_RECORDS", existRecords);
                }

                userDocument.append("RECORDS", tableData.values().stream()
                                                        .flatMap(Collection::stream)
                                                        .toList());

                return sb.append(new BoldString("\nВы уверены, что хотите продолжить?"))
                         .toString();
            }

            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, Document document) {
                builder.replyMarkup(YES_NO.build());
            }

            @Override
            public String validate(long userId, String userText, Document document) {
                return userText.equals(YES) || userText.equals(NO)
                        ? userText
                        : null;
            }

            @Override
            public String errorText() {
                return "Да или нет...";
            }

        },
        ACCEPT {
            @Override
            public void finalAction(long userId, @Nullable String userText, Document document) {
                if (document.getString(READ.name()).equals(YES)) {

                    Optional.ofNullable(document.getList("REMOVE_RECORDS", HospitalRecord.class))
                            .ifPresent(repositoryService::deleteAllRecords);

                    repositoryService.saveRecords(document.getList("RECORDS", HospitalRecord.class));
                }
            }

            @Override
            public void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, Document document) {
                builder.replyMarkup(new ReplyKeyboardRemove(true));
            }

            @Override
            public String getAnswer(@Nullable String userText, @Nonnull Document document) {
                return document.getString(READ.name()).equals(YES)
                        ? "Успешно выполнено"
                        : "Успешно ничего не сделано";
            }
        };

        private static RepositoryService repositoryService;

    }
}
