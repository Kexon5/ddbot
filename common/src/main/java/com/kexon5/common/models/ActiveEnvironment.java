package com.kexon5.common.models;


import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@Setter
@Builder
@Document(collection = "ACTIVE_ENVIRONMENTS")
public class ActiveEnvironment {

    @Id
    private ObjectId id;

    @Field("ENV")
    private String env;

    @Field("IS_MAIN")
    private boolean isMain;
}
