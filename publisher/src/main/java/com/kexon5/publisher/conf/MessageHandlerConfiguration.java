package com.kexon5.publisher.conf;

import com.kexon5.publisher.handlers.MessageHandler;
import com.kexon5.publisher.handlers.UpdateHandler;
import com.kexon5.publisher.handlers.WebSocketHandlerWithName;
import com.kexon5.publisher.service.UpdateService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerAdapter;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Configuration
public class MessageHandlerConfiguration {
    @Bean
    public HandlerAdapter wsHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    public Map<String, WebSocketHandler> handlers(UpdateService updateService) {
        return Stream.of(new UpdateHandler(updateService),
                         new MessageHandler(updateService, SendMessage.PATH),
                         new MessageHandler(updateService, EditMessageText.PATH),
                         new MessageHandler(updateService, DeleteMessage.PATH))
                     .collect(Collectors.toMap(WebSocketHandlerWithName::getPath, Function.identity()));
    }

    @Bean
    public HandlerMapping handlerMapping(Map<String, WebSocketHandler> handlers) {
        return new SimpleUrlHandlerMapping(handlers, -1);
    }
}
