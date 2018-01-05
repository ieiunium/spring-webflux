package com.epam.brest.flux.handler;

import static org.springframework.web.reactive.function.BodyInserters.fromPublisher;

import com.epam.brest.flux.dao.UserMongoReactiveRepository;
import com.epam.brest.flux.model.User;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.context.annotation.Profile;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.net.URI;

@Slf4j
@Service
@Profile("functional")
public class UserReactiveHandler {
    private final UserMongoReactiveRepository userRepository;

    public UserReactiveHandler(UserMongoReactiveRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Mono<ServerResponse> getAllUsers (ServerRequest request) {
        Flux<User> userFlux = userRepository.findAll();
        return ServerResponse.ok().contentType(MediaType.TEXT_EVENT_STREAM).body(fromPublisher(userFlux, User.class));
    }

    public Mono<ServerResponse> createUser (ServerRequest request) {
        Mono<User> userToCreate = request
                .bodyToMono(User.class)
                .doOnError(error -> log.error("Shit happens: {}", error.getMessage()));
        Mono<User> createdUser = userRepository.insert(userToCreate.block());
        UriComponentsBuilder uriBuilder = UriComponentsBuilder.fromUriString("/user/{id}");
        URI createdUserUri = uriBuilder.build(createdUser.block().getId());
        return ServerResponse.created(createdUserUri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(fromPublisher(createdUser, User.class));
    }

    public Mono<ServerResponse> updateUser (ServerRequest request) {
        Mono<User> userToUpdate = request
                .bodyToMono(User.class)
                .doOnError(error -> log.error("Shit happens: {}", error.getMessage()));
        Mono<User> updatedUser = userRepository.save(userToUpdate.block());
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(updatedUser, User.class);
    }

    public Mono<ServerResponse> deleteUser (ServerRequest request) {
        ObjectId userId = new ObjectId(request.pathVariable("id"));
        Mono<Void> deleted = userRepository.deleteById(userId);
        return ServerResponse.ok().body(deleted, Void.class);
    }

    public Mono<ServerResponse> getUserById (ServerRequest request) {
        ObjectId userId = new ObjectId(request.pathVariable("id"));
        return userRepository.findById(userId).flatMap(user -> ServerResponse.ok().body(Mono.just(user), User.class))
                .switchIfEmpty(ServerResponse.notFound().build());
    }
}
