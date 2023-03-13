package com.kexon5.ddbot.repositories;

import com.kexon5.ddbot.models.google.GoogleSetting;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GoogleSettingRepository extends MongoRepository<GoogleSetting, ObjectId> {

    GoogleSetting findByName(String name);
}
