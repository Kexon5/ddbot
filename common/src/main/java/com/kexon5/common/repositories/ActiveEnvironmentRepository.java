package com.kexon5.common.repositories;


import com.kexon5.common.models.ActiveEnvironment;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface ActiveEnvironmentRepository extends MongoRepository<ActiveEnvironment, ObjectId> {

    Optional<ActiveEnvironment> findByEnv(String env);

    void deleteByEnv(String env);

}
