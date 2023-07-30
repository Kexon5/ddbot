package com.kexon5.common.models;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.tuple.MutableTriple;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Builder
@Document(collection = "USERS")
public class User {

    public enum UserRecordStatus {
        REGISTERED,
        BAD_MISSED,
        GOOD_MISSED,
        PASSED
    }

    @Id
    private ObjectId id;

    @Field("USER_ID")
    private long userId;

    @Field("USERNAME")
    private String username;

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

    @Field("RECORDS")
    private List<MutableTriple<ObjectId, UserRecordStatus, String>> records = new ArrayList<>();

    @Field("ROLES")
    private Set<Role> roles;

    @Field("SETTINGS")
    private UserSettings userSettings;


    public String toShortString() {
        return name + ", Дата рождения: " + birthday + ", Роли: " + roles;
    }


    public void addRecord(ObjectId recordId) {
        records.add(MutableTriple.of(recordId, UserRecordStatus.REGISTERED, ""));
    }

    public void removeRecord(ObjectId recordId) {
        records.remove(records.size() - 1);
    }

    public boolean isNotificationEnabled() {
        return userSettings.isNotificationEnabled();
    }

}
