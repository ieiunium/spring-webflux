package com.epam.brest.flux.dao;

import com.epam.brest.flux.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IUserDao {
    Mono<User> getUserById(int userId);
    Mono<User> createUser(User user);
    Mono<User> updateUser(User user);
    Flux<User> getAllUsers();
    Mono<Void> deleteUserById(int userId);
}
