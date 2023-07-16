package com.kexon5.publisher.service;

import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Optional;

@RequiredArgsConstructor
public class UpdateSenderService extends UpdateService {

    public void addUpdate(Update update) {
        Optional.ofNullable(getUserEnv(update))
                .ifPresentOrElse(env -> env2UpdateUnicast.get(env).onNext(update), () -> envUnavailable(update, true));
    }

    @Override
    public Optional<String> execute(String method, String path) {
        return sender.execute(method, path);
    }

}
