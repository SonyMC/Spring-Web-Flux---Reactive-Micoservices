package com.reactivespring.controller;

import com.reactivespring.client.MoviesInfoRestClient;
import com.reactivespring.client.ReviewsRestClient;
import com.reactivespring.domain.Movie;
import com.reactivespring.domain.MovieInfo;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/v1/movies")
public class MoviesController {

    private MoviesInfoRestClient moviesInfoRestClient;

    private ReviewsRestClient reviewsRestClient;


    // auto inject
    public MoviesController(MoviesInfoRestClient moviesInfoRestClient, ReviewsRestClient reviewsRestClient) {
        this.moviesInfoRestClient = moviesInfoRestClient;
        this.reviewsRestClient = reviewsRestClient;
    }

    @GetMapping("/{id}")   // wil lbe passed as a path variable in the request
    public Mono<Movie>  retrieveMoviebyId(@PathVariable("id") String movieId) {

       // 1. Create a Movie using movieInfo and review
       // 2. Return  Mono<movie>
        return moviesInfoRestClient.retrieveMovieInfo(movieId) //// first call the movieInfosRestClient:Anytime a transformation needs ot be done on a reactive type, use a flatmap
                .flatMap(movieInfo -> {  // we are using flat map as we need to transform the flux returned below from retrieveReview to a mono
                     var reviewsListMono = reviewsRestClient.retrieveReview(movieId)
                                                                              .collectList();  // collectList() will return a Mono<List<>>


                    // Create a Mono<Movie> using Mono<List<Review>> using map operators
                    var movieMono = reviewsListMono.map(  // take each Review from List and create a Mono
                            reviews ->  new Movie(movieInfo, reviews));  // we have used @AllArgsConstructor in Movies.java

                    return movieMono;

                });


    }



    @GetMapping(value = "/stream", produces = MediaType.APPLICATION_NDJSON_VALUE)
    public Flux<MovieInfo> retrieveMovieInfo(){
        return moviesInfoRestClient.retrieveMovieInfoStream();
    }


}
