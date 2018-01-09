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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.DatabasePopulator;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;
import reactor.ipc.netty.NettyContext;
import reactor.ipc.netty.http.server.HttpServer;

import javax.sql.DataSource;

@Configuration
@ComponentScan(basePackages = {"com.epam.brest.flux.controller",
        "com.epam.brest.flux.handler",
        "com.epam.brest.flux.dao.jdbc"})
@EnableWebFlux
@Profile("jdbc")
@PropertySource("classpath:test-db.properties")
public class TestJDBCConfig {
    @Value("classpath:test-tables-init.sql")
    private Resource schemaScript;
    @Value("classpath:test-tables-populate.sql")
    private Resource dataPopulationScript;

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

    @Autowired
    private Environment env;

    @Bean
    public HttpHandler httpHandlerAnnotation(ApplicationContext context) {
        return WebHttpHandlerBuilder.applicationContext(context).build();
    }

    @Bean
    public NettyContext nettyContext(HttpHandler handler) {
        ReactorHttpHandlerAdapter adapter = new ReactorHttpHandlerAdapter(handler);
        HttpServer httpServer = HttpServer.create("localhost", 8090);
        return httpServer.newHandler(adapter).block();
    }

    @Bean
    DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("jdbc.driver"));
        dataSource.setUrl(env.getProperty("jdbc.url"));
        dataSource.setUsername(env.getProperty("jdbc.username"));
        dataSource.setPassword(env.getProperty("jdbc.password"));
        return dataSource;
    }

    @Bean
    @DependsOn("dataSource")
    DataSourceInitializer initializer(final DataSource dataSource) {
        final DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(databasePopulator());
        return initializer;
    }

    private DatabasePopulator databasePopulator() {
        final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(schemaScript);
        populator.addScript(dataPopulationScript);
        return populator;
    }

    @Bean
    PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(dataSource());
    }

    @Bean
    NamedParameterJdbcOperations namedParameterJdbcTemplate() {
        return new NamedParameterJdbcTemplate(dataSource());
    }


}
