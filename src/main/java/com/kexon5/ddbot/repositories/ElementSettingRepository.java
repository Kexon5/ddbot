package com.kexon5.ddbot.repositories;

import com.kexon5.ddbot.models.ElementSetting;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface ElementSettingRepository extends MongoRepository<ElementSetting, ObjectId> {

    List<ElementSetting> findAllByType(ElementSetting.Type type);
    ElementSetting findByElementName(String elementName);

    boolean existsByElementName(String elementName);

    default boolean isWorking(String elementName, ElementSetting.Type type) {
        return findByElementName(elementName).isWorking();

    }

    default void createNew(String elementName, ElementSetting.Type type) {
        ElementSetting newElementSetting = new ElementSetting(elementName, type);
        save(newElementSetting);
    }
}
