package com.kexon5.common.models;

import lombok.Getter;
import lombok.Setter;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashSet;
import java.util.Set;

@Getter
@Setter
@Document(collection = "MAILING_GROUPS")
public class MailingGroup {

    @Id
    private ObjectId id;

    @Field("GROUP_NAME")
    private String groupName;

    @Field("USERS")
    private Set<Long> users = new HashSet<>();



    public boolean addUser(long userId) {
        return users.add(userId);
    }

    public boolean removeUser(long userId) {
        return users.remove(userId);
    }

}