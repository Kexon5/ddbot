package com.kexon5.publisher.service;

import lombok.Setter;
import org.telegram.abilitybots.api.sender.SilentSender;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Flux;

import java.util.*;

import static org.telegram.abilitybots.api.objects.Flag.CALLBACK_QUERY;
import static org.telegram.abilitybots.api.util.AbilityUtils.getChatId;

public abstract class UpdateService {

    @Setter
    protected SilentSender sender;

    protected final Map<Long, String> user2Env = new HashMap<>();
    protected final Map<String, UpdateUnicaster> env2UpdateUnicast = new HashMap<>();

    protected final Set<Long> usersWhoFacedWithUnavailableBot = new HashSet<>();

    protected String mainEnv;

    public abstract Optional<String> execute(String method, String path);

    public abstract void addUpdate(Update update);

    public void addEnv(String env, boolean isMain) {
        if (isMain) {
            mainEnv = env;
            usersWhoFacedWithUnavailableBot.forEach(this::envAvailable);
            usersWhoFacedWithUnavailableBot.clear();
        }

        env2UpdateUnicast.put(env, new UpdateUnicaster());
    }

    public Flux<Update> getMessages(String env) {
        return env2UpdateUnicast.get(env).getMessages();
    }

    public void addUpdates(List<Update> updates) {
        updates.forEach(this::addUpdate);
    }

    public void removeEnv(String env, boolean isMain) {
        if (isMain) {
            mainEnv = null;
        }

        env2UpdateUnicast.remove(env);
    }

    public String getUserEnv(Update update) {
        long userId = getChatId(update);
        if (CALLBACK_QUERY.test(update) && env2UpdateUnicast.containsKey(update.getCallbackQuery().getData())) {
            setUserSpecificEnv(userId, update.getCallbackQuery().getData().toLowerCase());
            sender.execute(DeleteMessage.builder()
                                        .chatId(userId)
                                        .messageId(update.getCallbackQuery().getMessage().getMessageId())
                                        .build());
        }

        return user2Env.getOrDefault(userId, mainEnv);
    }

    public boolean setUserSpecificEnv(long userId, String env) {
        return user2Env.put(userId, env) == null;
    }

    public void envAvailable(long id) {
        sender.execute(SendMessage.builder()
                          .text("Рад Вам сообщить, что бот снова в строю!")
                          .chatId(id)
                          .build());
    }

    public void envUnavailable(Update update, boolean isMainEnv) {
        long userId = getChatId(update);

        usersWhoFacedWithUnavailableBot.add(userId);
        user2Env.remove(userId);
        sender.execute(SendMessage.builder()
                          .text("В данный момент бот на обсуживании\nИзвините за неудобства❤\uFE0F" + (!isMainEnv ? "Вы будете перенаправлены на основной хост" : ""))
                          .chatId(userId)
                          .build());
    }

}
