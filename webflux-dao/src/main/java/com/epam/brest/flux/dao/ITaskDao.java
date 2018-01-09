package com.epam.brest.flux.dao;

import com.epam.brest.flux.model.Task;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ITaskDao {
    Mono<Task> getTaskById(int userId);
    Mono<Task> createTask(Task task, Integer ownerId);
    Mono<Task> updateTask(Task task, Integer ownerId);
    Flux<Task> getAllTasks();
    Flux<Task> getAllTasksOfAUser(int userId);
    Mono<Void> deleteTaskById(int userId);
}
