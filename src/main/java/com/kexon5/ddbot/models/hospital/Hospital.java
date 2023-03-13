package com.kexon5.ddbot.models.hospital;

import com.kexon5.ddbot.utils.markup.BoldString;
import com.kexon5.ddbot.utils.markup.MarkupList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendLocation;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Document(collection = "HOSPITALS")
public class Hospital {
    @Id
    private ObjectId id;

    @Field("NAME")
    private String name;
    @Field("SIMPLE_NAME")
    private String simpleName;
    @Field("COORDINATES")
    private List<Double> coords;
    @Field("HOW_TO_GET")
    private MarkupList<String> howToGet;
    @Field("REQUIRED_DOCUMENTS")
    private MarkupList<String> requiredDocuments;
    @Field("NOTES")
    private MarkupList<String> notes;
    @Field("CONTACT_INFO")
    private MarkupList<String> contactInfo;

    public List<BotApiMethodMessage> getMessage(Long chatId) {
        return List.of(getLocation(chatId), getPlaceInfo(chatId));
    }

    public SendLocation getLocation(Long chatId) {
        return SendLocation.builder()
                .chatId(chatId)
                .longitude(coords.get(0))
                .latitude(coords.get(1))
                .build();
    }

    public SendMessage getPlaceInfo(Long chatId) {
        return SendMessage.builder()
                .chatId(chatId)
                .text(getPlaceInfoText(true))
                .build();
    }

    public String getPlaceInfoText(boolean forAdmins) {
        StringBuilder sb = new StringBuilder().append(name).append("\n");
        addList(sb, requiredDocuments, "Не забудьте взять с собой документы:");
        addList(sb, howToGet, "Как добраться:");
        if (forAdmins) {
            addList(sb, contactInfo, "Контактные данные больницы:");
            addList(sb, notes, "Примечания:");
        }
        return sb.toString();
    }

    private void addList(StringBuilder sb, List<String> listData, String boldMsg) {
        if (listData != null)
            sb.append("\n")
                    .append(new BoldString(boldMsg))
                    .append("\n\n")
                    .append(new MarkupList<>(listData))
                    .append("\n");
    }

    public void updateData(String[] requiredDocuments, String[] howToGet, String[] notes, String[] contactInfo) {
        updateField(this.requiredDocuments, requiredDocuments);
        updateField(this.howToGet, howToGet);
        updateField(this.notes, notes);
        updateField(this.contactInfo, contactInfo);
    }

    private static void updateField(List<String> listData, String[] newData) {
        if (newData != null) {
            listData.clear();
            listData.addAll(List.of(newData));
        }
    }

    public static List<Object> getSimpleStringList(List<Hospital> hospitals, Function<Hospital, MarkupList<String>> map) {
        return hospitals.stream()
                .map(map)
                .map(MarkupList::toSimpleString)
                .collect(Collectors.toList());
    }
}
