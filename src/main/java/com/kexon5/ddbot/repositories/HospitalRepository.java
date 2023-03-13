package com.kexon5.ddbot.repositories;

import com.kexon5.ddbot.models.hospital.Hospital;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface HospitalRepository extends MongoRepository<Hospital, ObjectId> {
    Hospital findBySimpleName(String simpleName);

}
