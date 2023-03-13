package com.kexon5.ddbot.repositories;

import com.kexon5.ddbot.models.hospital.HospitalRecord;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface HospitalRecordRepository extends MongoRepository<HospitalRecord, ObjectId> {
    boolean existsHospitalRecordByDateBetweenAndUsersContains(LocalDateTime date, LocalDateTime date2, List<Long> users);

    HospitalRecord findFirstByDateBetweenAndUsersContains(LocalDateTime date, LocalDateTime date2, List<Long> users);
}
