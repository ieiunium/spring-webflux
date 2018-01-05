package com.epam.brest.flux.app;

import com.epam.brest.flux.model.Task;
import com.epam.brest.flux.model.User;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.util.function.Tuple2;

public class TestZipStreams {
    public static void main(String[] args) {
        WebClient webClient = WebClient.create("http://localhost:8090");




    }
}
