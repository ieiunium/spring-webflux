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

import com.epam.brest.flux.handler.TaskReactiveHandler;
import com.epam.brest.flux.handler.UserReactiveHandler;
import com.mongodb.ConnectionString;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.AbstractReactiveMongoConfiguration;
import org.springframework.data.mongodb.repository.config.EnableReactiveMongoRepositories;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.core.publisher.Flux;
import reactor.ipc.netty.NettyContext;
import reactor.ipc.netty.http.server.HttpServer;

@Configuration
@EnableReactiveMongoRepositories(basePackages = {"com.epam.brest.flux.dao"})
@ComponentScan(basePackages = {"com.epam.brest.flux.*"})
@EnableWebFlux
public class ReactiveConfig extends AbstractReactiveMongoConfiguration {

    @Override
    protected String getDatabaseName() {
        return "spring_reactive";
    }

    @Override
    public MongoClient reactiveMongoClient() {
        MongoClient client = MongoClients.create(new ConnectionString("mongodb://localhost:27017"));
        Flux.from(client.listDatabaseNames()).doOnEach(System.out::println);
        return client;
    }

    @Bean
    @Profile("functional")
    public HttpHandler httpHandlerFunctional(RouterFunction<ServerResponse> router) {
        return RouterFunctions.toHttpHandler(router);
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

    @Bean
    @Profile("annotation")
    public HttpHandler httpHandlerAnnotation(ApplicationContext context) {
        return WebHttpHandlerBuilder.applicationContext(context).build();
    }

    @Bean
    public NettyContext nettyContext(HttpHandler handler) {
        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler);
        HttpServer httpServer = HttpServer.create("localhost", 8090);
        return httpServer.newHandler(adapter).block();
    }
}