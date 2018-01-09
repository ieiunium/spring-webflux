package com.epam.brest.flux.handler;

import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;

import com.epam.brest.flux.dao.TaskMongoReactiveRepository;
import com.epam.brest.flux.dao.UserMongoReactiveRepository;
import com.epam.brest.flux.model.Task;
import com.epam.brest.flux.model.User;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@Profile("functional")
public class TaskReactiveHandler {
    private final TaskMongoReactiveRepository taskRepository;
    private final UserMongoReactiveRepository userRepository;

    public TaskReactiveHandler(TaskMongoReactiveRepository taskRepository, UserMongoReactiveRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    public Mono<ServerResponse> getAllTasks(ServerRequest request) {
        Flux<Task> taskFlux = taskRepository.findAll();
        return ServerResponse.ok().contentType(MediaType.TEXT_EVENT_STREAM).body(fromPublisher(taskFlux, Task.class));
    }

    public Mono<ServerResponse> createTask(ServerRequest request) {
        ObjectId ownerId = new ObjectId(request.queryParam("id").orElse(""));
        Mono<User> owner = userRepository.findById(ownerId);
        Mono<Task> taskToCreate = request
                .bodyToMono(Task.class)
                .map(task -> {
                    task.setOwner(owner.block());
                    return task;
                })
                .doOnError(error -> log.error("Shit happens: {}", error.getMessage()));
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(taskRepository.insert(taskToCreate), Task.class);
    }

    public Mono<ServerResponse> updateTask(ServerRequest request) {
        Mono<Task> taskToUpdate = request
                .bodyToMono(Task.class)
                .doOnError(error -> log.error("Shit happens: {}", error.getMessage()));
        Mono<Task> updatedTask = taskRepository.insert(taskToUpdate).next();
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(updatedTask, Task.class);
    }

    public Mono<ServerResponse> deleteTask(ServerRequest request) {
        ObjectId taskId = new ObjectId(request.pathVariable("id"));
        Mono<Void> deleted = taskRepository.deleteById(taskId);
        return ServerResponse.ok().body(deleted, Void.class);
    }

    public Mono<ServerResponse> getTaskById(ServerRequest request) {
        ObjectId taskId = new ObjectId(request.pathVariable("id"));
        return taskRepository.findById(taskId).flatMap(task -> ServerResponse.ok().body(Mono.just(task), Task.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
