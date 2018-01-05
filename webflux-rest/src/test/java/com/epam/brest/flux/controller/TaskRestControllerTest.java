package com.epam.brest.flux.controller;

import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.reset;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertNull;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;

import com.epam.brest.flux.config.TestConfig;
import com.epam.brest.flux.dao.TaskMongoReactiveRepository;
import com.epam.brest.flux.dao.UserMongoReactiveRepository;
import com.epam.brest.flux.model.Task;
import com.epam.brest.flux.model.User;
import org.bson.types.ObjectId;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple2;

import java.time.LocalDate;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestConfig.class})
@ActiveProfiles("annotation")
public class TaskRestControllerTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(TaskRestControllerTest.class);

    private final ObjectId ID_1 = new ObjectId();
    private final ObjectId ID_2 = new ObjectId();
    private final ObjectId ID_3 = new ObjectId();
    private final ObjectId ID_4 = new ObjectId();
    private final User OWNER = new User(ID_4, "TEST");
    private final Task TASK_1 = Task.builder()
            .id(ID_1)
            .created(LocalDate.now().minusDays(1))
            .deadLine(LocalDate.now().plusDays(1))
            .description("Test Desc 1")
            .title("Test Title")
            .build();
    private final Task TASK_2 = Task.builder()
            .id(ID_2)
            .created(LocalDate.now().minusDays(1))
            .deadLine(LocalDate.now().plusDays(1))
            .description("Test Desc 2")
            .title("Test Title")
            .build();
    private final Task TASK_3 = Task.builder()
            .id(ID_3)
            .created(LocalDate.now().minusDays(1))
            .deadLine(LocalDate.now().plusDays(1))
            .description("Test Desc 3")
            .title("Test Title")
            .build();

    private WebTestClient webTestClient;
    @Autowired
    private ApplicationContext context;
    @Autowired
    protected UserMongoReactiveRepository userRepository;
    @Autowired
    protected TaskMongoReactiveRepository taskRepository;

    @Before
    public void setUp() throws Exception {
        webTestClient = WebTestClient.bindToApplicationContext(context).build();
        reset(userRepository, taskRepository);
    }

    @After
    public void tearDown() throws Exception {
        verify(userRepository, taskRepository);
    }

    @Test
    public void getAllTasks() {
        Flux<Task> testTasks = Flux.just(TASK_1, TASK_2, TASK_3);
        expect(taskRepository.findAll()).andReturn(testTasks);

        replay(taskRepository, userRepository);

        Flux<Task> testResult = webTestClient.get()
                .uri("/task/all")
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Task.class)
                .getResponseBody();

        StepVerifier.create(testResult)
                .expectNext(TASK_1)
                .expectNext(TASK_2)
                .expectNext(TASK_3)
                .expectComplete()
                .verifyThenAssertThat()
                .hasNotDroppedErrors();
    }

    @Test
    public void getAllTasksOfUser() {
        String userId = ID_4.toHexString();

        expect(taskRepository.findTasksByOwnerId(ID_4)).andReturn(Flux.just(TASK_2, TASK_1));

        replay(taskRepository, userRepository);

        Flux<Task> expectedTasks = webTestClient.get()
                .uri("/task/user/{id}", userId)
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Task.class)
                .getResponseBody();

        StepVerifier.create(expectedTasks)
                .expectNext(TASK_2)
                .expectNext(TASK_1)
                .expectComplete()
                .verifyThenAssertThat()
                .hasNotDroppedErrors();
    }

    @Test
    public void getTaskById() {
        String idString = TASK_1.getId().toHexString();
        Mono<Task> testTask = Mono.just(TASK_1);
        expect(taskRepository.findById(ID_1)).andReturn(testTask);

        replay(taskRepository, userRepository);

        Flux<Task> testResult = webTestClient.get()
                .uri("/task/{id}", idString)
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Task.class)
                .getResponseBody();

        StepVerifier.create(testResult)
                .expectNext(TASK_1)
                .expectComplete()
                .verifyThenAssertThat()
                .hasNotDroppedErrors();
    }

    @Test
    public void createTask() {
        String ownerId = ID_4.toHexString();
        assertNull(TASK_1.getOwner());
        expect(userRepository.findById(ID_4)).andReturn(Mono.just(OWNER));
        TASK_1.setOwner(OWNER);
        expect(taskRepository.insert(TASK_1)).andReturn(Mono.just(TASK_1));

        replay(taskRepository, userRepository);

        Flux<Task> createdTask = webTestClient
                .post()
                .uri(UriComponentsBuilder.fromUriString("/task").queryParam("id", ownerId).build().toUri())
                .accept(APPLICATION_JSON)
                .body(Mono.just(TASK_1), Task.class)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Task.class)
                .getResponseBody();

        StepVerifier.create(createdTask)
                .expectNext(TASK_1)
                .expectComplete()
                .verifyThenAssertThat()
                .hasNotDroppedErrors();

    }

    @Test
    public void updateTask() {
        expect(taskRepository.insert(TASK_1)).andReturn(Mono.just(TASK_1));

        replay(taskRepository, userRepository);

        Flux<Task> createdTask = webTestClient
                .put()
                .uri("/task")
                .accept(APPLICATION_JSON)
                .body(Mono.just(TASK_1), Task.class)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Task.class)
                .getResponseBody();

        StepVerifier.create(createdTask)
                .expectNext(TASK_1)
                .expectComplete()
                .verifyThenAssertThat()
                .hasNotDroppedErrors();

    }

    @Test
    public void deleteTask() {
        expect(taskRepository.deleteById(TASK_1.getId())).andReturn(Mono.create(MonoSink::success));

        replay(taskRepository, userRepository);

        Flux<Void> createdTask = webTestClient
                .delete()
                .uri("/task/{id}", TASK_1.getId().toHexString())
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

    @Test
    @Ignore
    public void testInfiniteFlux() {
        Flux<Task> tasks = webTestClient
                .get()
                .uri("/task/inf")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .returnResult(Task.class)
                .getResponseBody();

        Flux<User> users = webTestClient
                .get()
                .uri("/user/inf")
                .accept(MediaType.TEXT_EVENT_STREAM)
                .exchange()
                .returnResult(User.class)
                .getResponseBody();

        Flux.zip(tasks, users).doOnEach(signal -> signal.get().getT1().setOwner(signal.get().getT2()))
                .map(Tuple2::getT1)
                .doOnEach(taskSignal -> LOGGER.debug(taskSignal.get().toString()))
                .subscribe();
    }
}