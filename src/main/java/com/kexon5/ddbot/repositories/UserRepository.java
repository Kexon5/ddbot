package com.kexon5.ddbot.repositories;

import com.kexon5.ddbot.models.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Set;

public interface UserRepository extends MongoRepository<User, ObjectId> {
    List<User> findAllByRolesContains(Set<String> roles);

    List<User> findAllByNameContainsIgnoreCase(String substring);

    User findByUserId(long userId);

    boolean existsByUserId(long userId);

}
