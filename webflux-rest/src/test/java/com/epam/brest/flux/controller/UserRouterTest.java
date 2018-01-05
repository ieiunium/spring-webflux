package com.epam.brest.flux.controller;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.springframework.http.MediaType.APPLICATION_JSON;

import com.epam.brest.flux.config.TestConfig;
import com.epam.brest.flux.dao.TaskMongoReactiveRepository;
import com.epam.brest.flux.dao.UserMongoReactiveRepository;
import com.epam.brest.flux.model.User;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.test.StepVerifier;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("functional")
public class UserRouterTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserRouterTest.class);
    private final ObjectId ID_1 = new ObjectId();
    private final ObjectId ID_2 = new ObjectId();
    private final ObjectId ID_3 = new ObjectId();
    private final User USER_1 = new User(ID_1, "TEST1");
    private final User USER_2 = new User(ID_2, "TEST2");
    private final User USER_3 = new User(ID_3, "TEST3");

    private WebTestClient webTestClient;
    @Autowired
    private RouterFunction<ServerResponse> routerFunction;
    @Autowired
    protected UserMongoReactiveRepository userRepository;
    @Autowired
    protected TaskMongoReactiveRepository taskRepository;

    @Before
    public void setUp() throws Exception {
        webTestClient = WebTestClient.bindToRouterFunction(routerFunction).build();
        reset(userRepository, taskRepository);
    }

    @After
    public void tearDown() throws Exception {
        verify(userRepository, taskRepository);
    }

    @Test
    public void getAllUsers() {
        Flux<User> testUsers = Flux.just(USER_1, USER_2, USER_3);
        expect(userRepository.findAll()).andReturn(testUsers);
        expect(userRepository.findAll()).andReturn(testUsers);

        replay(taskRepository, userRepository);

        LOGGER.debug("Started 1st request");

        Flux<User> testResult = webTestClient.get()
                .uri("/user/all")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .returnResult(User.class)
                .getResponseBody();

        LOGGER.debug("Started 2nd request");

        Flux<User> testResult2 = webTestClient.get()
                .uri("/user/all")
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .returnResult(User.class)
                .getResponseBody();

        LOGGER.debug("Started 2nd verification");
        StepVerifier.create(testResult2)
                .expectNext(USER_1)
                .expectNext(USER_2)
                .expectNext(USER_3)
                .expectComplete()
                .verifyThenAssertThat()
                .hasNotDroppedErrors();

        LOGGER.debug("Started 1st verification");
        StepVerifier.create(testResult)
                .expectNext(USER_1)
                .expectNext(USER_2)
                .expectNext(USER_3)
                .expectComplete()
                .verifyThenAssertThat()
                .hasNotDroppedErrors();
    }

    @Test
    public void getUserById() {
        String idString = USER_1.getId().toHexString();
        Mono<User> testUser = Mono.just(USER_1);
        expect(userRepository.findById(ID_1)).andReturn(testUser);

        replay(taskRepository, userRepository);

        Flux<User> testResult = webTestClient.get()
                .uri("/user/{id}", idString)
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .returnResult(User.class)
                .getResponseBody();

        StepVerifier.create(testResult)
                .expectNext(USER_1)
                .expectComplete()
                .verifyThenAssertThat()
                .hasNotDroppedErrors();
    }

    @Test
    public void createUser() {
        expect(userRepository.insert(USER_3)).andReturn(Mono.just(USER_3));

        replay(taskRepository, userRepository);

        Flux<User> createdTask = webTestClient
                .post()
                .uri("/user")
                .accept(APPLICATION_JSON)
                .body(Mono.just(USER_3), User.class)
                .exchange()
                .expectStatus().isCreated()
                .returnResult(User.class)
                .getResponseBody();

        StepVerifier.create(createdTask)
                .expectNext(USER_3)
                .expectComplete()
                .verifyThenAssertThat()
                .hasNotDroppedErrors();

    }

    @Test
    public void updateUser() {
        expect(userRepository.save(USER_2)).andReturn(Mono.just(USER_2));

        replay(taskRepository, userRepository);

        Flux<User> updatedUser = webTestClient
                .put()
                .uri("/user")
                .accept(APPLICATION_JSON)
                .body(Mono.just(USER_2), User.class)
                .exchange()
                .expectStatus().isOk()
                .returnResult(User.class)
                .getResponseBody();

        StepVerifier.create(updatedUser)
                .expectNext(USER_2)
                .expectComplete()
                .verifyThenAssertThat()
                .hasNotDroppedErrors();

    }

    @Test
    public void deleteUser() {
        expect(userRepository.deleteById(USER_1.getId())).andReturn(Mono.create(MonoSink::success));

        replay(taskRepository, userRepository);

        Flux<Void> createdTask = webTestClient
                .delete()
                .uri("/user/{id}", USER_1.getId().toHexString())
                .accept(APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Void.class)
                .getResponseBody();

        StepVerifier.create(createdTask)
                .expectComplete()
                .verifyThenAssertThat()
                .hasNotDroppedErrors();
    }
}