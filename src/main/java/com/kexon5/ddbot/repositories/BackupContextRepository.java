package com.kexon5.ddbot.repositories;

import com.kexon5.ddbot.models.BackupContext;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;

public interface BackupContextRepository extends MongoRepository<BackupContext, ObjectId> {

    void deleteAllByDateTimeBefore(LocalDateTime time);
}
