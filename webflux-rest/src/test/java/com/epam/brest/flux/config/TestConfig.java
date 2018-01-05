package com.epam.brest.flux.config;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.web.reactive.function.server.RequestPredicates.DELETE;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.PUT;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RequestPredicates.queryParam;
import static org.springframework.web.reactive.function.server.RouterFunctions.nest;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

import com.epam.brest.flux.dao.TaskMongoReactiveRepository;
import com.epam.brest.flux.dao.UserMongoReactiveRepository;
import com.epam.brest.flux.handler.TaskReactiveHandler;
import com.epam.brest.flux.handler.UserReactiveHandler;
import org.easymock.EasyMock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

@Configuration
@ComponentScan(basePackages = {"com.epam.brest.flux.controller", "com.epam.brest.flux.handler"})
@EnableWebFlux
public class TestConfig {
    @Bean
    public UserMongoReactiveRepository userRepository() {
        return EasyMock.createMock(UserMongoReactiveRepository.class);
    }

    @Bean
    public TaskMongoReactiveRepository taskRepository() {
        return EasyMock.createMock(TaskMongoReactiveRepository.class);
    }

    @Bean
    @Profile("functional")
    public RouterFunction<ServerResponse> routerFunction(UserReactiveHandler userReactiveHandler,
                                                         TaskReactiveHandler taskReactiveHandler) {
        return nest(path("/user"),
                route(GET("/all").and(accept(APPLICATION_JSON)), userReactiveHandler::getAllUsers)
                        .andRoute(GET("/{id}").and(accept(APPLICATION_JSON)), userReactiveHandler::getUserById)
                        .andRoute(POST("").and(accept(APPLICATION_JSON)), userReactiveHandler::createUser)
                        .andRoute(PUT("").and(accept(APPLICATION_JSON)), userReactiveHandler::updateUser)
                        .andRoute(DELETE("/{id}").and(accept(APPLICATION_JSON)), userReactiveHandler::deleteUser))
                .andNest(path("/task"),
                        route(GET("/all").and(accept(APPLICATION_JSON)), taskReactiveHandler::getAllTasks)
                                .andRoute(GET("/{id}").and(accept(APPLICATION_JSON)), taskReactiveHandler::getTaskById)
                                .andRoute(POST("").and(queryParam("id", s -> s.length() > 0).and(accept(APPLICATION_JSON))), taskReactiveHandler::createTask)
                                .andRoute(PUT("").and(accept(APPLICATION_JSON)), taskReactiveHandler::updateTask)
                                .andRoute(DELETE("/{id}").and(accept(APPLICATION_JSON)), taskReactiveHandler::deleteTask));
    }

}
