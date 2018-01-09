package com.epam.brest.flux.controller;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.http.MediaType.TEXT_EVENT_STREAM;

import com.epam.brest.flux.config.TestJDBCConfig;
import com.epam.brest.flux.model.Task;
import com.epam.brest.flux.model.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {TestJDBCConfig.class})
@ActiveProfiles({"jdbc"})
@Transactional
public class TaskJDBSRestControllerTest {
    private WebTestClient webTestClient;

    @Autowired
    private ApplicationContext context;


    @Before
    public void setUp() throws Exception {
        webTestClient = WebTestClient.bindToApplicationContext(context).build();
    }

    @Test
    public void deleteTask() {
        List<Task> tasksBefore = webTestClient.get()
                .uri("/task/all")
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Task.class)
                .getResponseBody()
                .collectList().block();
        Random rnd = new Random();
        Integer idToDelete = tasksBefore.get(rnd.nextInt(tasksBefore.size())).getTaskId();

        Flux<Task> testResult = webTestClient.delete()
                .uri("/task/{id}", idToDelete)
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Task.class)
                .getResponseBody();

        StepVerifier.create(testResult)
                .expectComplete()
                .verifyThenAssertThat()
                .hasNotDroppedErrors();

        Flux<Task> testResultAfter = webTestClient.get()
                .uri("/task/all")
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Task.class)
                .getResponseBody();

        StepVerifier.create(testResultAfter)
                .expectNextCount(3)
                .expectComplete()
                .verifyThenAssertThat()
                .hasNotDroppedErrors();

    }

    @Test
    public void getAllTasks() {
        Flux<Task> testResult = webTestClient.get()
                .uri("/task/all")
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Task.class)
                .getResponseBody();

        StepVerifier.create(testResult)
                .expectNextCount(4)
                .expectComplete()
                .verifyThenAssertThat()
                .hasNotDroppedErrors();
    }

    @Test
    public void createTask() {
        Task testTask = Task.builder()
                .created(LocalDate.now())
                .deadLine(LocalDate.now().plusDays(1))
                .title("TestTitle")
                .description("TestDescription")
                .build();
        List<User> users = webTestClient.get()
                .uri("/user/all")
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(User.class)
                .getResponseBody()
                .collectList().block();

        Random rnd = new Random();
        Integer ownerId = users.get(rnd.nextInt(users.size())).getUserId();
        Flux<Task> createdTask = webTestClient
                .post()
                .uri(UriComponentsBuilder.fromUriString("/task").queryParam("userId", ownerId).build().toUri())
                .body(Mono.just(testTask), Task.class)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Task.class)
                .getResponseBody();

        Integer[] id = new Integer[1];

        StepVerifier.create(createdTask)
                .assertNext(testTask1 -> {
                    assertNotNull(testTask1.getTaskId());
                    id[0] = testTask1.getTaskId();
                })
                .expectComplete()
                .verifyThenAssertThat()
                .hasNotDroppedErrors();

        List<Task> tasksAfter = webTestClient.get()
                .uri("/task/all")
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Task.class)
                .getResponseBody()
                .collectList().block();

        assertEquals(5, tasksAfter.size());

/*        Flux<Task> testResult = webTestClient.delete()
                .uri("/task/{id}", id)
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Task.class)
                .getResponseBody();

        StepVerifier.create(testResult)
                .expectComplete()
                .verifyThenAssertThat()
                .hasNotDroppedErrors();

        tasksAfter = webTestClient.get()
                .uri("/task/all")
                .accept(TEXT_EVENT_STREAM)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Task.class)
                .getResponseBody()
                .collectList().block();

        assertEquals(4, tasksAfter.size());*/
    }

}
