package com.kexon5.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import reactor.core.publisher.Mono;

public class Serializer {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public <T> Mono<String> write(T o) {
        try {
            return Mono.just(objectMapper.writeValueAsString(o));
        } catch (JsonProcessingException e) {
            return Mono.error(e);
        }
    }

    public <T> Mono<T> read(String text, Class<T> tClass) {
        try {
            return Mono.just(objectMapper.readValue(text, tClass));
        } catch (JsonProcessingException ex) {
            return Mono.empty();
        }
    }

    public <T> T deserialize(String o, Class<T> tClass) {
        try {
            return objectMapper.readValue(o, tClass);
        } catch (JsonProcessingException e) {
            return null;
        }
    }
}
