package com.kexon5.bot.repositories;

import com.kexon5.bot.models.BackupContext;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;

public interface BackupContextRepository extends MongoRepository<BackupContext, ObjectId> {

    void deleteAllByDateTimeBefore(LocalDateTime time);
}
