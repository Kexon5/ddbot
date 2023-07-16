package com.kexon5.bot.repositories;

import com.kexon5.bot.models.hospital.Hospital;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HospitalRepository extends MongoRepository<Hospital, ObjectId> {
    Hospital findBySimpleName(String simpleName);

}
