package com.kexon5.ddbot.bot.services;

import com.kexon5.ddbot.statemachine.DialogueFlow;
import com.kexon5.ddbot.statemachine.MenuElement;
import com.kexon5.ddbot.statemachine.MessageState;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static org.telegram.abilitybots.api.objects.Flag.CALLBACK_QUERY;
import static org.telegram.abilitybots.api.objects.Flag.MESSAGE;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

@Slf4j
@Getter
public class ActionElement extends MenuElement {

    protected static final String USER_CONTEXT = "user_contexts";

    private final ActionState actionState;

    protected static final Map<Long, Document> contextMap = new HashMap<>();

    private final ActionMessageState[] steps;


    protected ActionElement(ActionState actionState, ActionMessageState[] steps) {
        this(actionState, null, steps);
    }

    protected ActionElement(ActionState actionState,
                            @Nullable Predicate<Long> accessPredicate,
                            ActionMessageState[] steps) {
        super(actionState, accessPredicate);

        this.actionState = actionState;
        this.steps = steps;
    }

    @Override
    public DialogueFlow.DialogueFlowBuilder setAdditional(DialogueFlow.DialogueFlowBuilder builder) {
        return builder
                .onlyIf(upd -> CALLBACK_QUERY.test(upd) && upd.getCallbackQuery().getData().equals(actionState.toString()));
    }

    private Update createEmptyUpdate(Update update) {
        Chat chat = new Chat();
        chat.setId(getChatId(update));

        Message msg = new Message();
        msg.setChat(chat);
        msg.setText("empty");

        Update newUpdate = new Update();
        newUpdate.setMessage(msg);
        return newUpdate;
    }

    @Override
    public DialogueFlow.DialogueFlowBuilder createReplyFlow() {
        Map<String, DialogueFlow.DialogueFlowBuilder> jumpMap = new HashMap<>();

        DialogueFlow.DialogueFlowBuilder workingBuilder = DialogueFlow.builder(dbContext)
                                                                    .postAction((bot, upd) -> bot.onUpdateReceived(createEmptyUpdate(upd)));

        for (int i = steps.length - 1; i >= 1; i--) {
            int finalI = i;

            Optional.ofNullable(steps[i - 1].jumpStateName())
                    .map(jumpMap::get)
                    .ifPresent(workingBuilder::jumpFlow);

            DialogueFlow reply = workingBuilder
                    .action((bot, update) -> {
                        boolean isFinal = finalI == steps.length - 1;
                        if (isFinal) {
                            steps[finalI].finalAction(update);
                        }
                        bot.silent().execute(steps[finalI].getMessage(update));
                        if (isFinal) {
                            contextMap.remove(getChatId(update));
                        }
                    })
                    .validate(steps[i - 1]::validate)
                    .errorAction(steps[i - 1].errorText())
                    .enableStats(steps[i].toString())
                    .build();

            jumpMap.put(steps[i].name(), workingBuilder);

            workingBuilder = DialogueFlow.builder(dbContext)
                                         .next(reply);
        }

        return workingBuilder
                .action((bot, update) -> {
                    long userId = getChatId(update);

                    steps[0].init(userId);
                    BotApiMethod<?> msg = steps[0].getMessage(update);

                    if (CALLBACK_QUERY.test(update)) {
                        bot.silent().execute(DeleteMessage.builder()
                                                          .chatId(userId)
                                                          .messageId(update.getCallbackQuery().getMessage().getMessageId())
                                                          .build());
                    }

                    bot.silent().execute(msg);
                })
                .enableStats(steps[0].toString());
    }


    public interface ActionMessageState extends MessageState {

        default void init(long userId) {
            Document userDocument = new Document();
            contextMap.put(userId, userDocument);
            initAction(userId, userDocument);
        }

        default void initAction(long userId, Document userDocument) {}

        default String errorText() {
            return "Error";
        }

        default String jumpStateName() {
            return null;
        }

        default String getAnswer(long userId, @Nullable String userText) {
            return getAnswer(userText, contextMap.get(userId));
        }

        default String getAnswer(@Nullable String userText, @Nonnull Document document) {
            return null;
        }

        @Override
        default void setOptionsToBuilder(long userId, SendMessage.SendMessageBuilder builder) {
            setOptionsToBuilder(builder, contextMap.get(userId));
        }

        default void setOptionsToBuilder(SendMessage.SendMessageBuilder builder, Document document) {}

        default void finalAction(Update update) {
            long userId = getChatId(update);
            finalAction(userId, MESSAGE.test(update) ? update.getMessage().getText() : null, contextMap.get(userId));
        }

        default void finalAction(long userId, @Nullable String userText, Document document) {}

        default <T> boolean validate(Update update) {
            try {
                T result;
                long userId = getChatId(update);
                if (MESSAGE.test(update)
                        && (result = validate(userId, update.getMessage().getText(), contextMap.get(userId))) != null) {
                    contextMap.get(userId).append(name(), result);
                    return true;
                }
            } catch (Exception ex) {
                log.info("Some validate exception: ", ex);
            }
            return false;
        }

        default <T> T validate(long userId, String userText, Document document) throws Exception {
            return null;
        }

        String name();
    }

}
