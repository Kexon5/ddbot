package com.kexon5.bot.models.google;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "GOOGLE_SETTINGS")
public class GoogleSetting {

    @Id
    private ObjectId id;

    @Field("NAME")
    private String name;

    @Field("GOOGLE_ID")
    private String googleId;

    @Field("LINK")
    private String link;

    public GoogleSetting(String name, String googleId, String link) {
        this.name = name;
        this.googleId = googleId;
        this.link = link;
    }

}



