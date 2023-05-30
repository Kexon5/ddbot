package com.kexon5.ddbot.repositories;

import com.kexon5.ddbot.models.hospital.HospitalRecord;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface HospitalRecordRepository extends MongoRepository<HospitalRecord, ObjectId> {
    boolean existsHospitalRecordByDateBetweenAndUsersContains(LocalDateTime date, LocalDateTime date2, List<Long> users);

    boolean existsHospitalRecordByStateEquals(HospitalRecord.RecordState state);

    HospitalRecord findFirstByDateBetweenAndUsersContains(LocalDateTime date, LocalDateTime date2, List<Long> users);

    List<HospitalRecord> findAllByDateBetween(LocalDateTime date1, LocalDateTime date2);

    void deleteAllByDateBetween(LocalDateTime date1, LocalDateTime date2);

    List<HospitalRecord> findAllByStateEquals(HospitalRecord.RecordState recordState);
}
