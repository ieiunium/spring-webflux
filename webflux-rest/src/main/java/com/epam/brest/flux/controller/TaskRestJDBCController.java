package com.epam.brest.flux.controller;

import com.epam.brest.flux.dao.ITaskDao;
import com.epam.brest.flux.dao.IUserDao;
import com.epam.brest.flux.model.Task;
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
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;
import java.util.Random;

@RestController
@Profile("jdbc")
@RequestMapping("/task")
public class TaskRestJDBCController {
    private final ITaskDao taskRepository;
    private final IUserDao userRepository;

    public TaskRestJDBCController(ITaskDao taskRepository, IUserDao userRepository) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/all")
    public Flux<Task> getAllTasks() {
        return taskRepository.getAllTasks()
                .onErrorResume(Flux::error)
                .publishOn(Schedulers.elastic());
    }

    @GetMapping("/user/{id}")
    public Flux<Task> getAllTasksOfUser(@PathVariable("id") Integer userId) {
        return taskRepository.getAllTasksOfAUser(userId);
    }

    @GetMapping("/{id}")
    public Mono<Task> getTaskById(@PathVariable("id") Integer id) {
        return taskRepository.getTaskById(id);
    }

    @PostMapping
    public Mono<Task> createTask(@RequestBody Mono<Task> taskMono, @RequestParam("userId") Integer userId) {
        return userRepository.getUserById(userId)
                .map(user -> taskRepository.createTask(taskMono.block(), user.getUserId()).block());
    }

    @PutMapping
    public Mono<Task> updateTask(@RequestBody Mono<Task> taskMono, @RequestParam("id") Integer userId) {
        return taskRepository.updateTask(taskMono.block(), userId);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteTask(@PathVariable("id") Integer id) {
        return taskRepository.deleteTaskById(id);
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
