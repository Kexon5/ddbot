package com.kexon5.bot.models.hospital;

import com.kexon5.bot.utils.markup.BoldString;
import com.kexon5.bot.utils.markup.MarkupList;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.kexon5.bot.bot.services.edithospital.actions.AddHospital.AddSteps.*;

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

    public void updateData(org.bson.Document userDocument) {
        updateField(this.requiredDocuments, userDocument.get(REQUIRED_DOCUMENTS, String[].class));
        updateField(this.howToGet, userDocument.get(HOW_TO_GET, String[].class));
        updateField(this.notes, userDocument.get(NOTES, String[].class));
        updateField(this.contactInfo, userDocument.get(CONTACT_INFO, String[].class));
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
