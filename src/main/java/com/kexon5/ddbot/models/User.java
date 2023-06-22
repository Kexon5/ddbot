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
    private String sex;

    @Field("PHONE_NUMBER")
    private String phoneNumber;

    @Field("GROUP_NUMBER")
    private String groupNumber;

    @Field("BLOOD_GROUP")
    private String bloodGroup;

    @Field("RH_FACTOR")
    private String rhFactor;

    @Field("KELL_FACTOR")
    private String kellFactor;

    @Field("ACTIVE_RECORD")
    private Integer activeRecord;

    @Field("ROLES")
    private Set<Roles> roles;


    public String toShortString() {
        return name + ", Дата рождения: " + birthday + ", Роли: " + roles;
    }
}
