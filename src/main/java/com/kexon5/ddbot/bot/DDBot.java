package com.kexon5.ddbot.bot;

import com.kexon5.ddbot.bot.services.mainmenu.MainMenuService;
import com.kexon5.ddbot.statemachine.DialogueFlow;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.ReplyCollection;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;


@Slf4j
@Getter
public class DDBot extends AbilityBot {

    private static final int RECONNECT_PAUSE = 10_000;

    @Value("${creatorId:-1}")
    private long creatorId;

    private final ReplyCollection actionReplyCollection;
    private final MainMenuService mainMenu;

    public DDBot(String botToken,
                 String botUsername,
                 ReplyCollection actionReplyCollection,
                 MainMenuService mainMenu) {
        super(botToken, botUsername);

        this.actionReplyCollection = actionReplyCollection;
        this.mainMenu = mainMenu;
    }

    public DialogueFlow getMenuFlow() {
        return mainMenu.getReplyFlowBuilder().build();
    }

    public void botConnect(TelegramBotsApi telegramBotsApi) {
        try {
            telegramBotsApi.registerBot(this);
            log.info("Bot registered!");
        } catch (TelegramApiException e) {
            log.error("Try to reconnect after " + RECONNECT_PAUSE / 1000 + "sec. Error: " + e.getMessage());
            try {
                Thread.sleep(RECONNECT_PAUSE);
            } catch (InterruptedException e1) {
                e1.printStackTrace();
                return;
            }
            botConnect(telegramBotsApi);
        } catch (Exception e) {
            log.error("Unknown error: ", e);
        }
    }

    @Override
    public long creatorId() {
        return creatorId;
    }
}