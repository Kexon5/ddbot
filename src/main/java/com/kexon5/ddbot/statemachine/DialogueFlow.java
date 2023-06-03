package com.kexon5.ddbot.statemachine;

import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.Reply;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.google.common.collect.Lists.newArrayList;
import static org.telegram.abilitybots.api.objects.Flag.CALLBACK_QUERY;
import static org.telegram.abilitybots.api.objects.Flag.MESSAGE;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

public class DialogueFlow extends Reply {

    private final Set<Reply> nextReplies;

    private DialogueFlow(List<Predicate<Update>> conditions, BiConsumer<BaseAbilityBot, Update> action, Set<Reply> nextReplies, String name) {
        super(conditions, action, name);
        this.nextReplies = nextReplies;
    }

    public static DialogueFlowBuilder builder(DBContext db) {
        return new DialogueFlowBuilder(db);
    }

    public static DialogueFlowBuilder builder(DBContext db, int id) {
        return new DialogueFlowBuilder(db, id);
    }

    public Set<Reply> nextReplies() {
        return nextReplies;
    }

    @Override
    public Stream<Reply> stream() {
        return Stream.concat(nextReplies.stream().flatMap(Reply::stream), Stream.of(this));
    }

    public static class DialogueFlowBuilder {
        public static final String STATES = "user_state_replies";
        public static final String ERROR_COUNTER = "user_errors";

        public static final int MAX_ERROR = 3;
        private static final AtomicInteger replyCounter = new AtomicInteger();
        private final DBContext db;
        private final int id;
        private final List<Predicate<Update>> conds;
        private Predicate<Update> validateCond = upd -> true;
        private BiConsumer<BaseAbilityBot, Update> action;
        private BiConsumer<BaseAbilityBot, Update> postAction;
        private BiConsumer<BaseAbilityBot, Update> errorAction = (bot, upd) -> {};
        private BiConsumer<BaseAbilityBot, Update> exitAction = sendMessage("Слишком много ошибок!\nВозвращение в главное меню")
                .andThen(BaseAbilityBot::onUpdateReceived);

        private final Set<Reply> nextReplies;
        private String name;

        private DialogueFlowBuilder jumpFlow;

        private DialogueFlowBuilder(DBContext db, int id) {
            this.conds = new ArrayList<>();
            this.nextReplies = new HashSet<>();
            this.db = db;
            this.id = id;
        }

        private DialogueFlowBuilder(DBContext db) {
            this(db, replyCounter.getAndIncrement());
        }

        public DialogueFlowBuilder action(BiConsumer<BaseAbilityBot, Update> action) {
            this.action = action;
            return this;
        }

        public DialogueFlowBuilder errorAction(BiConsumer<BaseAbilityBot, Update> errorAction) {
            this.errorAction = errorAction;
            return this;
        }

        public DialogueFlowBuilder errorAction(String text) {
            this.errorAction = sendMessage(text);
            return this;
        }

        public DialogueFlowBuilder exitAction(BiConsumer<BaseAbilityBot, Update> exitAction) {
            this.exitAction = exitAction;
            return this;
        }

        public DialogueFlowBuilder exitAction(String text) {
            this.exitAction = sendMessage(text);
            return this;
        }

        public DialogueFlowBuilder jumpFlow(DialogueFlowBuilder builder) {
            this.jumpFlow = builder;
            return this;
        }

        public DialogueFlowBuilder postAction(BiConsumer<BaseAbilityBot, Update> action) {
            this.postAction = action;
            return this;
        }

        public DialogueFlowBuilder enableStats(String name) {
            this.name = name;
            return this;
        }

        public DialogueFlowBuilder validate(Predicate<Update> pred) {
            this.validateCond = pred;
            return this;
        }

        public DialogueFlowBuilder onlyIf(Predicate<Update> pred) {
            conds.add(pred);
            return this;
        }

        public DialogueFlowBuilder next(DialogueFlow nextDialogueFlow) {
            List<Predicate<Update>> statefulConditions = toStateful(nextDialogueFlow.conditions(), nextDialogueFlow.name());

            DialogueFlow statefulDialogueFlow = new DialogueFlow(statefulConditions, nextDialogueFlow.action(), nextDialogueFlow.nextReplies(), nextDialogueFlow.name());
            nextReplies.add(statefulDialogueFlow);
            return this;
        }

        private static void runAction(DialogueFlowBuilder builder,
                                      Long chatId,
                                      BaseAbilityBot bot,
                                      Update upd) {
            builder.action.accept(bot, upd);
            if (builder.nextReplies.size() > 0 || CALLBACK_QUERY.test(upd)) {
                builder.db.<Long, Integer>getMap(STATES).put(chatId, builder.id);
            }
        }

        public DialogueFlow build() {
            if (action == null)
                action = (bot, upd) -> {};

            BiConsumer<BaseAbilityBot, Update> statefulAction = (bot, upd) -> {
                Long chatId = getChatId(upd);

                if (validateCond.test(upd)) {
                    runAction(this, chatId, bot, upd);
                } else {
                    if (jumpFlow != null) {
                        runAction(jumpFlow, chatId, bot, upd);
                        if (jumpFlow.postAction != null) {
                            db.<Long, Integer>getMap(STATES).remove(getChatId(upd));
                            jumpFlow.postAction.accept(bot, upd);
                        }
                    } else {
                        Map<Long, Integer> errorMap = db.getMap(ERROR_COUNTER);
                        int errorNumber = errorMap.getOrDefault(chatId, 1);
                        if (errorNumber < MAX_ERROR) {
                            errorAction.accept(bot, upd);
                            errorMap.put(chatId, errorNumber + 1);
                        } else {
                            errorMap.remove(chatId);
                            exitAction.accept(bot, upd);
                        }
                    }
                }
            };

            if (postAction != null) {
                statefulAction = statefulAction
                        .andThen((bot, upd) -> db.<Long, Integer>getMap(STATES).remove(getChatId(upd)))
                        .andThen(postAction);
            }

            return new DialogueFlow(conds, statefulAction, nextReplies, name);
        }

        public int getUserStateId(Update upd) {
            Long chatId = getChatId(upd);
            return db.<Long, Integer>getMap(STATES).getOrDefault(chatId, -1);
        }

        private static BiConsumer<BaseAbilityBot, Update> sendMessage(String text) {
            return (bot, upd) -> bot.silent().execute(SendMessage.builder()
                                                                 .chatId(getChatId(upd))
                                                                 .text(text)
                                                                 .build());
        }

        @Nonnull
        private List<Predicate<Update>> toStateful(List<Predicate<Update>> conditions, String name) {
            List<Predicate<Update>> statefulConditions = newArrayList(conditions);
            statefulConditions.add(0, upd -> {
                int stateId = getUserStateId(upd);

                return MESSAGE.test(upd) && name.endsWith("MENU")
                        ? false
                        : CALLBACK_QUERY.test(upd)
                            ? upd.getCallbackQuery().getData().equals(name)
                            : id == stateId;
            });
            return statefulConditions;
        }
    }
}
