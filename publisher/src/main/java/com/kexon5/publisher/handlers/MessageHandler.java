package com.kexon5.publisher.handlers;

import com.kexon5.publisher.service.UpdateService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class MessageHandler implements WebSocketHandlerWithName {

    private final UpdateService updateService;
    private final String path;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.receive()
                      .flatMap(message -> session.send(Mono.justOrEmpty(updateService.execute(message.getPayloadAsText(), path))
                      .map(session::textMessage)))
                .then();
    }


    @Override
    public String getPath() {
        return "/" + path;
    }
}
