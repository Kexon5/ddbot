package com.kexon5.bot.bot;

import com.kexon5.bot.services.MethodUnicaster;
import com.kexon5.common.Serializer;
import com.kexon5.common.models.ActiveEnvironment;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import org.springframework.web.reactive.socket.client.ReactorNettyWebSocketClient;
import org.springframework.web.reactive.socket.client.WebSocketClient;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Serializable;
import java.net.URI;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

@Slf4j
@RequiredArgsConstructor
public class BotWebsocketClient {

    private static final Serializer serializer = new Serializer();

    private final DDBot bot;

    private final ActiveEnvironment activeEnvironment;
    private final MethodUnicaster methodUnicaster;
    private final String url;

    private final WebSocketClient client = new ReactorNettyWebSocketClient();
    private final ExecutorService exe;

    public void init() {
        updateClient();
        clientConnect(SendMessage.PATH, session -> stream(session, SendMessage.PATH));
        clientConnect(EditMessageText.PATH, session -> stream(session, EditMessageText.PATH));
        clientConnect(DeleteMessage.PATH, session -> session.send(msgSend(session, methodUnicaster.getMessages(DeleteMessage.PATH))));
    }


    private static void wait(TimeUnit timeUnit, long timeout) {
        try {
            timeUnit.sleep(timeout);
        } catch (InterruptedException ignored) {}
    }

    private Mono<Void> stream(WebSocketSession session, String methodPath) {
        Flux<Pair<BotApiMethod<?>, Consumer<Serializable>>> pairs = methodUnicaster.getPairs(methodPath);

        Flux<BotApiMethod<?>> msgs = pairs.map(Pair::getLeft);
        Flux<Consumer<Serializable>> callbacks = pairs.map(Pair::getRight);

        return session.send(msgSend(session, msgs))
                      .and(Flux.zip(callbacks, session.receive()
                                                      .flatMap(message -> serializer.read(message.getPayloadAsText(), Message.class)))
                               .doOnNext(tuple -> tuple.getT1().accept(tuple.getT2())));
    }

    public void updateClient() {
        URI uri = URI.create(String.format(url + "/updates?env=%s&isMain=%b",
                                           activeEnvironment.getEnv(),
                                           activeEnvironment.isMain()
        ));

        client.execute(uri,
                       session -> session.receive()
                                         .doOnNext(message -> {
                                                 Update update = serializer.deserialize(message.getPayloadAsText(), Update.class);
                                                 exe.submit(() -> bot.onUpdateReceived(update));
                                         }).then()
              )
                .doOnError(err -> {})
                .doOnTerminate(() -> {
                    wait(TimeUnit.SECONDS, 5);
                    updateClient();
                })
              .subscribe();
    }

    private Flux<WebSocketMessage> msgSend(WebSocketSession session, Flux<? extends BotApiMethod<?>> messages) {
        return messages.flatMap(serializer::write)
                .map(session::textMessage);
    }


    public void clientConnect(String methodPath, WebSocketHandler webSocketHandler) {
        URI uri = URI.create(url + "/" + methodPath);

        client.execute(uri, webSocketHandler)
              .doOnError(err -> {})
              .doOnTerminate(() -> {
                  wait(TimeUnit.SECONDS, 5);
                  clientConnect(methodPath, webSocketHandler);
              })
              .subscribe();
    }

}
