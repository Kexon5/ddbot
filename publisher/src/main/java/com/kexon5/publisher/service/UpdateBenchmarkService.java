package com.kexon5.publisher.service;

import lombok.extern.slf4j.Slf4j;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.time.Duration;
import java.util.Optional;

@Slf4j
public class UpdateBenchmarkService extends UpdateService {

    private long start;

    public void addUpdate(Update update) {
        start = System.currentTimeMillis();
        Optional.ofNullable(getUserEnv(update))
                .ifPresentOrElse(env -> env2UpdateUnicast.get(env).onNext(update), () -> envUnavailable(update, false));
    }

    public Optional<String> execute(String method, String path) {
        Optional<String> message = sender.execute(method, path);
        log.info("Reply elapsed time {}", Duration.ofMillis(System.currentTimeMillis() - start));
        return message;
    }


}
