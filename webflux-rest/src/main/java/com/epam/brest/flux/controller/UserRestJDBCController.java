package com.epam.brest.flux.controller;

import com.epam.brest.flux.dao.jdbc.TaskDao;
import com.epam.brest.flux.dao.jdbc.UserDao;
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
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@Profile("jdbc")
@RequestMapping("/user")
public class UserRestJDBCController {
    private final UserDao userRepository;
    private final TaskDao taskRepository;

    public UserRestJDBCController(UserDao userRepository, TaskDao taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    @GetMapping("/all")
    public Flux<User> getAllTasks() {
        return userRepository.getAllUsers();
    }

    @GetMapping("/task/{id}")
    public Mono<User> getTaskOwner (@PathVariable("id") Integer userId) {
        return taskRepository.getTaskById(userId).map(Task::getOwner);
    }

    @GetMapping("/{id}")
        public Mono<User> getUserById(@PathVariable("id") Integer id) {
        return userRepository.getUserById(id);
    }

    @PostMapping
    public Mono<User> createUser(@RequestBody Mono<User> userMono) {
        return userRepository.createUser(userMono.block());
    }

    @PutMapping
    public Mono<User> updateUser(@RequestBody Mono<User> userMono) {
        return userRepository.updateUser(userMono.block());
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteUser(@PathVariable("id") Integer id) {
        return userRepository.deleteUserById(id);
    }

    @GetMapping("/inf")
    public Flux<User> infiniteUsers() {
        Faker faker = new Faker();
        return Flux.generate(userSynchronousSink -> userSynchronousSink.next(User.builder()
        .id(new ObjectId())
        .userName(faker.name().fullName())
                .build()
        ));
    }
}
