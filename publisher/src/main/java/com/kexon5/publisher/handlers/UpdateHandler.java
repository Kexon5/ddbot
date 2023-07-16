package com.kexon5.publisher.handlers;

import com.kexon5.common.Serializer;
import com.kexon5.publisher.service.UpdateService;
import lombok.RequiredArgsConstructor;
import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URLEncodedUtils;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.List;

@RequiredArgsConstructor
public class UpdateHandler implements WebSocketHandlerWithName {

    private static final Serializer serializer = new Serializer();

    private final UpdateService updateService;

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        List<NameValuePair> params = URLEncodedUtils.parse(session.getHandshakeInfo().getUri().getQuery(), StandardCharsets.UTF_8);
        String env = params.get(0).getValue();
        boolean isMain = Boolean.parseBoolean(params.get(1).getValue());
        updateService.addEnv(env, isMain);

        return session.send(updateService.getMessages(env)
                                         .flatMap(serializer::write)
                                         .map(session::textMessage))
                .doOnTerminate(() -> updateService.removeEnv(env, isMain));
    }

    @Override
    public String getPath() {
        return "/updates";
    }
}
