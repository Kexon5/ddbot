package com.kexon5.common.repositories;

import com.kexon5.common.models.MailingGroup;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MailingGroupRepository extends MongoRepository<MailingGroup, ObjectId> {

    MailingGroup findByGroupName(String userGroup);

}
