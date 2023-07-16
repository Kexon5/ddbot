package com.kexon5.publisher.handlers;


import org.springframework.web.reactive.socket.WebSocketHandler;

public interface WebSocketHandlerWithName extends WebSocketHandler {

    String getPath();
}
