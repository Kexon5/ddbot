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

@Configuration
public class MessageHandlerConfiguration {
    @Bean
    public HandlerAdapter wsHandlerAdapter() {
        return new WebSocketHandlerAdapter();
    }

    @Bean
    public UpdateHandler updateHandler(UpdateService updateService) {
        return new UpdateHandler(updateService);
    }

    @Bean
    public MessageHandler sendMessageHandler(UpdateService updateService) {
        return new MessageHandler(updateService, SendMessage.PATH);
    }

    @Bean
    public MessageHandler editMessageHandler(UpdateService updateService) {
        return new MessageHandler(updateService, EditMessageText.PATH);
    }

    @Bean
    public MessageHandler deleteMessageHandler(UpdateService updateService) {
        return new MessageHandler(updateService, DeleteMessage.PATH);
    }


    @Bean
    public Map<String, WebSocketHandler> handlers(UpdateHandler updateHandler,
                                                  MessageHandler sendMessageHandler,
                                                  MessageHandler editMessageHandler,
                                                  MessageHandler deleteMessageHandler) {
        return Map.of("/updates", updateHandler,
                      sendMessageHandler.getPath(), sendMessageHandler,
                      editMessageHandler.getPath(), editMessageHandler,
                      deleteMessageHandler.getPath(), deleteMessageHandler
        );
    }

    @Bean
    public HandlerMapping handlerMapping(List<WebSocketHandlerWithName> handlers) {
        Map<String, WebSocketHandler> handlerMap = handlers.stream()
                                                           .collect(Collectors.toMap(WebSocketHandlerWithName::getPath, Function.identity()));

        return new SimpleUrlHandlerMapping(handlerMap, -1);
    }
}
