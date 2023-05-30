package com.kexon5.ddbot.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.Set;

@Getter
@Setter
@Builder
@Document(collection = "USERS")
public class User {

    @Id
    private ObjectId id;

    @Field("USER_ID")
    private long userId;

    @Field("NAME")
    private String name;

    @Field("BIRTHDAY")
    private LocalDate birthday;

    @Field("SEX")
    private boolean isMan;

    @Field("PHONE_NUMBER")
    private String phoneNumber;

    @Field("GROUP_NUMBER")
    private String groupNumber;

    @Field("BLOOD_GROUP")
    private int bloodGroup;

    @Field("RH_FACTOR")
    private boolean isRhPositive;

    @Field("KELL_FACTOR")
    private boolean isKellPositive;

    @Field("ACTIVE_RECORD")
    private ObjectId activeRecord;

    @Field("ROLES")
    private Set<String> roles;
}
