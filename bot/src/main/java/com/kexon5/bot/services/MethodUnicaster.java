package com.kexon5.bot.services;

import org.apache.commons.lang3.tuple.Pair;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

import java.io.Serializable;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MethodUnicaster {

    private final Map<String, Sinks.Many<Pair<BotApiMethod<?>, Consumer<Serializable>>>> processorMap =
            Stream.of(SendMessage.PATH, EditMessageText.PATH, DeleteMessage.PATH)
                  .collect(Collectors.toMap(Function.identity(), e -> Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false)));

    public <T extends Serializable, Method extends BotApiMethod<T>> void onNext(Method next, Consumer<T> callback) {
        processorMap.get(next.getMethod())
                    .emitNext(
                            Pair.of(next, (Consumer<Serializable>) callback),
                            Sinks.EmitFailureHandler.busyLooping(Duration.ofMillis(100))
                    );
    }

    public Flux<Pair<BotApiMethod<?>, Consumer<Serializable>>> getPairs(String method) {
        return processorMap.get(method).asFlux();
    }

    public Flux<BotApiMethod<?>> getMessages(String method) {
        return getPairs(method).map(Pair::getLeft);
    }

}
