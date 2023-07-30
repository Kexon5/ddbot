package com.kexon5.bot.bot;

import com.kexon5.bot.bot.services.mainmenu.MainMenuService;
import com.kexon5.bot.services.MethodUnicaster;
import com.kexon5.common.statemachine.DialogueFlow;
import com.kexon5.common.statemachine.MessageHolder;
import lombok.Getter;
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

import static com.kexon5.common.statemachine.DialogueFlow.DialogueFlowBuilder.msgs;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;


@Getter
public class DDBot extends AbilityBot {

    private final long creatorId;

    private final ReplyCollection actionReplyCollection;
    private final DialogueFlow mainMenu;
    private final MethodUnicaster methodUnicaster;

    public DDBot(String botUsername,
                 DBContext dbContext,
                 DefaultBotOptions options,
                 long creatorId,
                 ReplyCollection actionReplyCollection,
                 MainMenuService mainMenu,
                 MethodUnicaster methodUnicaster) {
        super("", botUsername, dbContext, options);

        this.creatorId = creatorId;
        this.actionReplyCollection = actionReplyCollection;
        this.mainMenu = mainMenu.getReplyFlowBuilder().build();
        this.methodUnicaster = methodUnicaster;
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