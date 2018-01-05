package com.epam.brest.flux.dao;

import com.epam.brest.flux.model.Task;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface TaskMongoReactiveRepository extends ReactiveMongoRepository<Task, ObjectId> {
    Flux<Task> findTasksByOwnerId(ObjectId userId);
}
