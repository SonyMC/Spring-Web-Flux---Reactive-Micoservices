package com.reactivespring.controller;

// Create a movieinfo into Mongo repository

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.service.MoviesInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.Valid;
import java.rmi.MarshalledObject;
import java.time.Duration;

@RestController
@RequestMapping("/v1")
@Slf4j
public class MoviesInfoController {

    // @Autowired   // We are using the constructor pattern here instead to auto inject. Refer below constructor code
    private MoviesInfoService moviesInfoService;



    //Publish & Subscribe : Sink publishes multiple events and will replay all events once a subscriber is connected to it
    Sinks.Many<MovieInfo> moviesInfoSink = Sinks.many().replay().all();  //// replay().all() : anytime a new subscriber is added it will replay al levents
    //Sinks.Many<MovieInfo> moviesInfoSink = Sinks.many().replay().latest(); //// replay().latest() -> First subscriber will get all and subsequently only the latest event will be streamed to all subscriber.


    //Generate constructor ( Right click -> Generate -> constructor) to inject MoviesInfoService into this class)
    // Alternate method is to use the autowired annotation in the moviesInfoService declaration
    public MoviesInfoController(MoviesInfoService moviesInfoService) {

        this.moviesInfoService = moviesInfoService;
    }

    // Post MovieInfo
    @PostMapping("/movieinfos")           // post url : localhost:8080/v1/moviesinfos
    @ResponseStatus(HttpStatus.CREATED)    // Http response to be expected
    public Mono<MovieInfo> addMovieInfo(@RequestBody @Valid MovieInfo movieInfo) {    // movieINfo needs to be provided by client

        return moviesInfoService.addMovieInfo(movieInfo)
                .log();

    }


    // Get all movies
    @GetMapping("/movieinfos")           // get url : localhost:8080/v1/moviesinfos
    public Flux<MovieInfo> getAllMovieInfos(){
        return moviesInfoService.getAllMovieInfos().log();

    }



    // Get a movie by an id
    @GetMapping("/movieinfos/{id}")           // get url : localhost:8080/v1/movieinfos/<id>
    public Mono<MovieInfo> getMovieInfoById(@PathVariable("id") String id){  // id will eb provided in request
        return moviesInfoService.getMovieInfoByID(id).log();

    }

    // Get a movie by an id - Approach 2
    @GetMapping("/movieinfos_2/{id}")           // get url : localhost:8080/v1/movieinfos_2/<id>
    public Mono<ResponseEntity<MovieInfo>> getMovieInfoById_approach2(@PathVariable("id") String id){  // id will be provided in request

        return moviesInfoService.getMovieInfoByID(id)
                .map(movieInfo1 -> ResponseEntity.ok()     // if response is ok
                        .body(movieInfo1))  // get movieInfo in response body
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build())) //switch to not found response entity if nothing is nothing is retrieved for given id
                .log();
    }



    // Get all movies for a year which is provided as a request parm
    @GetMapping("/movieinfosForYear")           // get url : localhost:8080/v1/movieinfosForYear; provide year as an optional  request parm
    public Flux<MovieInfo> getAllMovieInfosForYear(@RequestParam(value = "year", required = false) Integer year){
        log.info("year: {}", year); // log year if provided

        // check if year is provided
        if(year != null){
            return moviesInfoService.getMovieInfoByYear(year).log();
        }

        // if year is not provided
        return moviesInfoService.getAllMovieInfos().log();

    }


    // Get a movie by name
    @GetMapping("/movieinfosForName/{name}")           // get url : localhost:8080/v1/moviesinfos/<name>
    public Mono<ResponseEntity<MovieInfo>> getMovieInfoByName(@PathVariable("name") String name){  // name will be provided in request

        return moviesInfoService.getMovieInfoByName(name)
                .map(movieInfo1 -> ResponseEntity.ok()     // if response is ok
                        .body(movieInfo1))  // get movieInfo in response body
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build())) //switch to not found response entity if nothing is nothing is retrieved for given id
                .log();
    }





    // Update a movie
    @PutMapping("movieinfos/{id}")           // url : localhost:8080/v1/moviesinfos/<id>
    @ResponseStatus(HttpStatus.OK)
    public Mono<MovieInfo> updateMovieInfo(@RequestBody @Valid MovieInfo movieInfo, @PathVariable String id){
        return moviesInfoService.updateMovieInfo(movieInfo,id);
    }

    // Update a movie - approach 2  : return not found response if provided id is not found
    @PutMapping("movieinfos_2/{id}")           //  url : localhost:8080/v1/moviesinfos_2/<id>
    @ResponseStatus(HttpStatus.OK)
    public Mono<ResponseEntity<MovieInfo>> updateMovieInfo_Approach2(@RequestBody @Valid MovieInfo movieInfo, @PathVariable String id){
        var updatedMovieInfoMono =  moviesInfoService.updateMovieInfo(movieInfo,id);
        return updatedMovieInfoMono
                .map(movieInfo1 -> ResponseEntity.ok()   //if response is ok
                                .body(movieInfo1))       // get response body for movieInfo
                .switchIfEmpty(Mono.just(ResponseEntity.notFound().build()));   //if response body is empty return not found!!!


    }



    @DeleteMapping("movieinfos/{id}")           // get url : localhost:8080/v1/moviesinfos/<id>
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Mono<Void> deleteMovieInfoById(@PathVariable String id){

        return moviesInfoService.deleteMovieInfoById(id);
    }



    // Post & Publish MovieInfo
    @PostMapping("/movieinfos_publish")           // post url : localhost:8080/v1/moviesinfos_publish
    @ResponseStatus(HttpStatus.CREATED)    // Http response to be expected
    public Mono<MovieInfo> addMovieInfo_publish(@RequestBody @Valid MovieInfo movieInfo){    // movieINfo needs to be provided by client


        // ** SSE ( Server Side Events) demo using Sink
        // 1. Publish the added movie info
        // 2. Expose teh date as SSE( ServerSentEvent) endpoint
        // 3. subscribe to the published movie-info
        // Note: Sink API can be used to act as both a Publisher and Subscriber. Refer : https://projectreactor.io/docs/core/release/reference/#processors
        // 4. Use Sink API to manually trigger a reactive stream.
        // 5. Sinks.one publishes only one event and Sink.may publishes many events
        // 6. Publish E.g.: sink.emitNext() or sink.tryEmitNext
        // 7. Subscribe E.g.: replaySink.asFlux()

        return moviesInfoService.addMovieInfo(movieInfo)
                // 1. Access the saved movie info using donOnNext() Operator
                // 2. Publish the Movie Info : Manual triggering using tryEmitNext()
                // 3. tryEventNext() automatically takes care of the Failure Handler and so no explicit FailureHandler is required
                .doOnNext(savedMovieInfo -> {
                    moviesInfoSink.tryEmitNext(savedMovieInfo);
                    //moviesInfoSink.tryEmitComplete();  // will stop publishing as soon as the first movie info is published
               })

                .log();

        // ** Note: The above snippet publishes the MovieInfo using sink. Now we will subscribe to it using a separate end point as bekow.


    }


    // Subscribe to the above published SSE
    // Client will connect to the url which will automatically subscribe to that particular sink which will continuously publish the movie info events
    // On hitting this url, it will keep on subscribing to the published events. On a browser this will keep on downloading the json file. In order to see a result uncomment 'moviesInfoSink.tryEmitComplete()' operator in addMovieInfo_publish() above. This will donload only one event.
    @GetMapping(value = "/movieinfos/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)    // Will download teh json file when url is opened in browser.Get url : localhost:8080/v1/movieinfos/stream
    public Flux<MovieInfo> getMovieInfoById_subscriber(){




            var movieInfoFlux  =  moviesInfoSink.asFlux()     // subscribe and return as Flux.
                    .take(Duration.ofSeconds(5)) // subscribe only for this duration. Can be commented out for continuous subscription.
                   // .take(5)      // subscribe only for the specified event count
                    .log();


            return movieInfoFlux;
       }


}
