package com.kexon5.bot.repositories;

import com.kexon5.bot.models.hospital.HospitalBackup;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HospitalBackupRepository extends MongoRepository<HospitalBackup, ObjectId> {
}
