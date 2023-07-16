package com.kexon5.publisher.service;

import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.util.concurrent.Queues;

import java.time.Duration;

public class UpdateUnicaster {

    private final Sinks.Many<Update> processor = Sinks.many().multicast().onBackpressureBuffer(Queues.SMALL_BUFFER_SIZE, false);

    public void onNext(Update next) {
        processor.emitNext(
                next,
                Sinks.EmitFailureHandler.busyLooping(Duration.ofMillis(100))
        );
    }

    public Flux<Update> getMessages() {
        return processor.asFlux();
    }
}
