package com.kexon5.bot.bot;

import com.kexon5.bot.bot.services.mainmenu.MainMenuService;
import com.kexon5.bot.statemachine.DialogueFlow;
import com.kexon5.bot.statemachine.MessageHolder;
import com.kexon5.bot.services.MethodUnicaster;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.ReplyCollection;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.io.Serializable;
import java.util.function.Consumer;

import static com.kexon5.bot.statemachine.DialogueFlow.DialogueFlowBuilder.msgs;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

@Slf4j
@Getter
public class DDBot extends AbilityBot {

    @Value("${creatorId:-1}")
    private long creatorId;

    private final ReplyCollection actionReplyCollection;
    private final MainMenuService mainMenu;
    private final MethodUnicaster methodUnicaster;

    public DDBot(String botUsername,
                 DefaultBotOptions options,
                 DBContext dbContext,
                 ReplyCollection actionReplyCollection,
                 MainMenuService mainMenu,
                 MethodUnicaster methodUnicaster) {
        super("", botUsername, dbContext, options);

        this.actionReplyCollection = actionReplyCollection;
        this.mainMenu = mainMenu;
        this.methodUnicaster = methodUnicaster;
    }

    public DialogueFlow getMenuFlow() {
        return mainMenu.getReplyFlowBuilder().build();
    }

    @Override
    public long creatorId() {
        return creatorId;
    }

    @Override
    public void sendDefaultMsg(Update update) {
        long userId = getChatId(update);
        MessageHolder lastMsg = msgs.get(userId);

        silent.executeAsync(DeleteMessage.builder()
                                      .chatId(userId)
                                      .messageId(lastMsg.getMsgId())
                                      .build(), consumer -> {});

        silent.executeAsync(lastMsg.getMsg(), response -> lastMsg.setMsgId(((Message)response).getMessageId()));
    }


    @Override
    protected <T extends Serializable, Method extends BotApiMethod<T>> void sendApiMethodAsync(Method method, Consumer<T> callback) {
        methodUnicaster.onNext(method, callback);
    }
}