package com.kexon5.bot.repositories;

import com.kexon5.bot.models.hospital.HospitalRecord;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Set;

public interface HospitalRecordRepository extends MongoRepository<HospitalRecord, ObjectId> {

    boolean existsHospitalRecordByStateEquals(HospitalRecord.RecordState state);

    List<HospitalRecord> findAllByStateEquals(HospitalRecord.RecordState recordState);

    List<HospitalRecord> findHospitalRecordsByRecordHashIn(Set<Integer> hashes);

    HospitalRecord findByRecordHash(Integer hash);

}
