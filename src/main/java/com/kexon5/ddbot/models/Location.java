package com.kexon5.ddbot.models;

import com.kexon5.ddbot.markup.BoldString;
import com.kexon5.ddbot.markup.MarkupList;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;

@Builder
@RequiredArgsConstructor
public class Location {
    @Getter
    private final String placeName;
    private final double latitude;
    private final double longitude;
    private final MarkupList<String> howToGet;
    private final String importantInfo;

    public Location(String placeName, double latitude, double longitude, List<String> howToGet, String importantInfo) {
        this.placeName = placeName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.howToGet = new MarkupList<>(howToGet);
        this.importantInfo = importantInfo;
    }

    public List<BotApiMethodMessage> getMessage(Long chatId) {
        return List.of(getLocation(chatId), getPlaceInfo(chatId));
    }

    public SendLocation getLocation(Long chatId) {
        return SendLocation.builder()
                .chatId(chatId)
                .longitude(longitude)
                .latitude(latitude)
                .build();
    }

    public SendMessage getPlaceInfo(Long chatId) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(getPlaceInfoText())
                .build();
    }

    private String getPlaceInfoText() {
        StringBuilder sb = new StringBuilder().append(placeName).append("\n");
        if (importantInfo != null) {
            sb.append("\n")
                    .append(new BoldString("Important info:"))
                    .append("\n")
                    .append(importantInfo)
                    .append("\n");
        }
        if (howToGet != null) {
            sb.append("\n")
                    .append(new BoldString("How to get:"))
                    .append("\n")
                    .append(howToGet)
                    .append("\n");
        }
        return sb.toString();
    }
}
