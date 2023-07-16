package com.kexon5.bot.models;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.Map;

@Document(collection = "BACKUP_CONTEXT")
public class BackupContext {

    @Id
    private ObjectId id;

    @Field("DATE")
    private LocalDateTime dateTime;

    @Field("USER_STATES")
    private Object userStates;

    @Field("USER_CONTEXTS")
    private Map<Long, org.bson.Document> userContexts;

    public BackupContext(Object userStates, Map<Long, org.bson.Document> userContexts) {
        this.dateTime = LocalDateTime.now();
        this.userStates = userStates;
        this.userContexts = userContexts;
    }

}
