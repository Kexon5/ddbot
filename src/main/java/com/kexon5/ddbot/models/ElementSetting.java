package com.kexon5.ddbot.models;


import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Setter
@Getter
@Document(collection = "ELEMENT_SETTINGS")
public class ElementSetting {

    public enum Type {
        ACTION,
        SERVICE
    }


    @Id
    private ObjectId id;

    @Field("ELEMENT_NAME")
    private String elementName;

    @Field("ELEMENT_TYPE")
    private Type type;

    @Field("WORKING")
    private boolean working;

    public ElementSetting(String elementName, Type type) {
        this.elementName = elementName;
        this.type = type;
        this.working = true;
    }

    public ElementSetting inverseWorking() {
        working = !working;
        return this;
    }

}
