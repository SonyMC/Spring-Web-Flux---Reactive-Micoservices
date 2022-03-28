package com.reactivespring.controller;


import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;

@RestController
public class FluxAndMonoController {

    // Return a simple flux for non blocking and asynchronous processing
    // url: http://localhost:8080/flux
    @GetMapping("/flux")
    public Flux<Integer> flux(){

        return Flux.just(1,2,3)
                .log();
    }

    // Return a simple mono for non blocking and asynchronous processing
    //url: http://localhost:8080/mono
    @GetMapping("/mono")
    public Mono<String> mono(){
        return Mono.just("Keep Roaring!!!")
                .log();
    }


    // Return a streaming endpoint
    //url: http://localhost:8080/stream
    // we need to explicitly mention the Media Type when using stream
    // MediaType.TEXT_EVENT_STREAM_VALUE instructs the endpoint to produce a stream of data to the client
    @GetMapping(value = "/stream", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<Long> stream(){
        return Flux.interval(Duration.ofSeconds(1))   // For every second , a nextValue() event is triggered
                .log();
    }
}
