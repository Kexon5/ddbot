package com.kexon5.common.repositories;

import com.kexon5.common.models.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepository extends MongoRepository<User, ObjectId> {
    List<User> findAllByRolesContains(Set<String> roles);

    List<User> findAllByNameContainsIgnoreCase(String substring);

    User findByUserId(long userId);

    Optional<User> searchByUserId(long userId);

    Set<User> findAllByUserIdIn(Set<Long> userIds);

    boolean existsByUserId(long userId);

}
