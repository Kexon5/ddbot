package com.kexon5.publisher.repositories;


import com.kexon5.publisher.models.Scenario;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ScenarioRepository extends MongoRepository<Scenario, ObjectId> {

    Scenario findByName(String name);

}
