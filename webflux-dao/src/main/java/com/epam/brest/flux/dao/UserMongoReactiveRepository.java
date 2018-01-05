package com.epam.brest.flux.dao;

import com.epam.brest.flux.model.User;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserMongoReactiveRepository extends ReactiveMongoRepository<User, ObjectId> {
}