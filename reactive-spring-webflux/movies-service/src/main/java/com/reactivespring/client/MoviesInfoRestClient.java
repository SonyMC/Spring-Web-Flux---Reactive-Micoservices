package com.reactivespring.client;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.exception.MoviesInfoClientException;
import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.util.RetryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class MoviesInfoRestClient {

    private WebClient webClient;

    @Value("${restClient.moviesInfoUrl}")   // key-value pair is specified in application.yaml
    private String moviesInfoUrl;

    @Value("${restClient.moviesInfoUrlSubscribe}") // key-value pair is specified in application.yaml
    private String moviesInfoSubscribeUrl;


 //*** Method of auto-injecting a webclient using a constructor

    public MoviesInfoRestClient(WebClient webClient){
        this.webClient = webClient;
    }





    public Mono<MovieInfo> retrieveMovieInfo(String movieId) {

        var url = moviesInfoUrl.concat("/stream"); // refer movies-info-service.MoviesInfoController.getMovieInfoById()

        //A RetryBackoffSpec preconfigured for fixed delays given a maximum number of retry attempts and the fixed Duration for the backoff.
        //var retrySpec = Retry.fixedDelay(3, Duration.ofSeconds(1));
        // The below extraction has been moved to RetryUtil.java
        /*
        var retrySpecWithFilter = Retry.fixedDelay(3, Duration.ofSeconds(1))
                .filter((ex) -> ex instanceof  MoviesInfoServerException)  // do the retry only for MovieInfo server not available
                .onRetryExhaustedThrow(((retryBackoffSpec, retrySignal) -> Exceptions.propagate(retrySignal.failure())));   // propagate root cause of issue to client
        */

        return webClient.get()
                .uri(url, movieId)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (clientResponse -> {      // onStatus is a function call to access error status ;we can then implement custom error handling
                    log.info("Status code : {}", clientResponse.statusCode().value());
                    if (clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)) {  // if movie id is not found throw mono error
                        return Mono.error(new MoviesInfoClientException("There is no MovieInfo available for the passed in Id : " + movieId, clientResponse.statusCode().value()));
                    }
                    return clientResponse.bodyToMono(String.class)     // for all other errors, show the error response and code
                            .flatMap(response -> Mono.error(new MoviesInfoClientException(response, clientResponse.statusCode().value())));
                }))
                .onStatus(HttpStatus::is5xxServerError, (clientResponse -> {      // onStatus is a function call to access error status ;we can then implement custom error handling
                    log.info("Status code : {}", clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class)     // for all other errors, show the error response and code
                            .flatMap(response -> Mono.error(new MoviesInfoServerException("Server Exception in MoviesInfoService" + response)));
                }))

                .bodyToMono(MovieInfo.class)
               // .retry(3)  // simple retry for 3 times when any of teh above exceptions(4xx, 5xx)  is thrown
               // .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(1)))  //A RetryBackoffSpec preconfigured for fixed delays given a maximum number of retry attempts and the fixed Duration for the backoff.
               // .retryWhen(retrySpec); // Moved the above parameter to a variable 'retrySpec'
               //.retryWhen(retrySpecWithFilter) // retrySpec variable with Filters
                .retryWhen(RetryUtil.retrySpec()) // moved retry code to a separate class for reusability
                .log();

    }


    public Flux<MovieInfo> retrieveMovieInfoStream() {


        return webClient.get()
                .uri(moviesInfoSubscribeUrl)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (clientResponse -> {      // onStatus is a function call to access error status ;we can then implement custom error handling
                    log.info("Status code : {}", clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class)     // show the error response and code
                            .flatMap(response -> Mono.error(new MoviesInfoClientException(response, clientResponse.statusCode().value())));
                }))
                .onStatus(HttpStatus::is5xxServerError, (clientResponse -> {      // onStatus is a function call to access error status ;we can then implement custom error handling
                    log.info("Status code : {}", clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class)     // for all other errors, show the error response and code
                            .flatMap(response -> Mono.error(new MoviesInfoServerException("Server Exception in MoviesInfoService" + response)));
                }))

                .bodyToFlux(MovieInfo.class)
                .retryWhen(RetryUtil.retrySpec()) // moved retry code to a separate class for reusability
                .log();

    }

}
