package com.epam.brest.flux.controller;

import com.epam.brest.flux.dao.TaskMongoReactiveRepository;
import com.epam.brest.flux.dao.UserMongoReactiveRepository;
import com.epam.brest.flux.model.Task;
import com.epam.brest.flux.model.User;
import com.github.javafaker.Faker;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.Random;

@RestController
@Profile("annotation")
@RequestMapping("/task")
public class TaskRestController {
    private final TaskMongoReactiveRepository taskRepository;
    private final UserMongoReactiveRepository userRepository;

    public TaskRestController(TaskMongoReactiveRepository taskRepository, UserMongoReactiveRepository userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/all")
    public Flux<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @GetMapping("/user/{id}")
    public Flux<Task> getAllTasksOfUser(@PathVariable("id") ObjectId userId) {
        return taskRepository.findTasksByOwnerId(userId);
    }

    @GetMapping("/{id}")
    public Mono<Task> getTaskById(@PathVariable("id") ObjectId id) {
        return taskRepository.findById(id);
    }

    @PostMapping
    public Mono<Task> createTask(@RequestBody Mono<Task> taskMono, @RequestParam("id") ObjectId userId) {
        User owner = userRepository.findById(userId).blockOptional().orElseThrow(RuntimeException::new);
        return taskRepository.insert(taskMono.map(task1 -> {
            task1.setOwner(owner);
            return task1;
        }).block());
    }

    @PutMapping
    public Mono<Task> updateTask(@RequestBody Mono<Task> taskMono) {
        return taskRepository.insert(taskMono.block());
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteTask(@PathVariable("id") ObjectId id) {
        return taskRepository.deleteById(id);
    }

    @GetMapping("/inf")
    public Flux<Task> infiniteTasks() {
        Random random = new Random();
        Faker faker = new Faker();
        return Flux.generate(taskSynchronousSink -> taskSynchronousSink.next(Task.builder()
                .id(new ObjectId())
                .created(LocalDate.now().minusDays(random.nextInt(10)))
                .deadLine(LocalDate.now().plusDays(random.nextInt(10)))
                .description(faker.beer().name())
                .title(faker.beer().malt())
                .build()));
    }
}
