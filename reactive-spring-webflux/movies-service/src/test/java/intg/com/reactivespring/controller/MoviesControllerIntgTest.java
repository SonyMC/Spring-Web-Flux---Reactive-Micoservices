package com.reactivespring.controller;


import com.github.tomakehurst.wiremock.client.WireMock;
import com.reactivespring.domain.Movie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.client.AutoConfigureWebClient;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;


import java.util.Objects;

import static com.github.tomakehurst.wiremock.client.WireMock.*;  // required for stubFor()
import static org.junit.jupiter.api.Assertions.assertEquals; // required fro assertEquals()

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)   // will spin up Spring app in a random port
@ActiveProfiles("test")   // Ensure this is different to all other profiles provided in  application.yml
@AutoConfigureWebTestClient
@AutoConfigureWireMock(port = 8084) //automatically spins a httpserver in port  8084
@TestPropertySource(properties = {
        "restClient.moviesInfoUrl=http://localhost:8084/v1/movieinfos_2",   // Override port definition in application.yml
        "restClient.reviewsUrl=http://localhost:8084/v1/reviews/qp"         // actual app runs on 8081
})
public class MoviesControllerIntgTest {

    @Autowired
    WebTestClient webTestClient;


    @BeforeEach
    void setup(){
        WireMock.reset();
    }


    @Test
    void retrieveMovieById(){

        //given
        var movieId = "abc";
        // stubFor is used to create a wiremock response for MoviesInfoRestClient.
        // call will first be made to MoviesInfoRestClient.retrieveMovieInfo via the .get in the //when section
        // MoviesInfoRestClient.java: the call will be transferred to the wiremock after .retrieve() in retrieveMovieInfo(String movieId) function
        stubFor(get(urlEqualTo("/v1/movieinfos_2/" + movieId))  // url is MovieInfo url with request parm
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));  // movieinfo.json is available in resources/__files. This will be provided as a response
        // stubFor is used to create a wiremock response for ReviewsRestClient.
        // csll will first be made to MoviesInfoRestClient.retrieveMovieInfo via the .get in the //when section
        // ReviewsRestClient.java: the call will be transferred to the wiremock after .retrieve() in retrieveReview(String movieId) function
        stubFor(get(urlPathEqualTo("/v1/reviews/qp"))  // url path is Review url for request parm
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));  // reviews.json is available in resources/__files. This will be provided as a response



        //when
        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)   // url of the Movie service
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)  // refer MovieController.java
                .consumeWith(movieEntityExchangeResult -> {  // get response body
                     var movie = movieEntityExchangeResult.getResponseBody();
                     assert Objects.requireNonNull(movie).getReviewList().size() == 2 ; // we have 2 reviews in reviews.json available in resources/__files
                     assertEquals("Batman Begins", movie.getMovieInfo().getName());
                });

    }


    @Test
    void retrieveMovieById_2(){

        //given
        var movieId = "abc";
        // stubFor is used to create a wiremock response for MoviesInfoRestClient.
        // call will first be made to MoviesInfoRestClient.retrieveMovieInfo via the .get in the //when section
        // MoviesInfoRestClient.java: the call will be transferred to the wiremock after .retrieve() in retrieveMovieInfo(String movieId) function
        stubFor(get(urlEqualTo("/v1/movieinfos_2/" + movieId))  // url is MovieInfo url with request parm
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));  // movieinfo.json is available in resources/__files. This will be provided as a response
        // stubFor is used to create a wiremock response for ReviewsRestClient.
        // csll will first be made to MoviesInfoRestClient.retrieveMovieInfo via the .get in the //when section
        // ReviewsRestClient.java: the call will be transferred to the wiremock after .retrieve() in retrieveReview(String movieId) function
        stubFor(get(urlPathEqualTo("/v1/reviews/qp"))  // url path is Review url for request parm
                .withQueryParam("%20movieInfoId", equalTo(movieId))  //Pass the Query Parm. For some reason we need to append a leading space encoded as %20 as else the Query parm will not work.
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("reviews.json")));  // reviews.json is available in resources/__files. This will be provided as a response



        //when
        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)   // url of the Movie service
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)  // refer MovieController.java
                .consumeWith(movieEntityExchangeResult -> {  // get response body
                    var movie = movieEntityExchangeResult.getResponseBody();
                    assert Objects.requireNonNull(movie).getReviewList().size() == 2 ; // we have 2 reviews in reviews.json available in resources/__files
                    assertEquals("Batman Begins", movie.getMovieInfo().getName());
                });

    }


    @Test
    void retrieveMovieById_404(){

        //given
        var movieId = "abc";
        // stubFor is used to create a wiremock response for MoviesInfoRestClient.
        // call will first be made to MoviesInfoRestClient.retrieveMovieInfo via the .get in the //when section
        // MoviesInfoRestClient.java: the call will be transferred to the wiremock after .retrieve() in retrieveMovieInfo(String movieId) function
        stubFor(get(urlEqualTo("/v1/movieinfos_2/" + movieId))  // url is MovieInfo url with request parm
                .willReturn(aResponse()
                        .withStatus(404)));
         // stubFor is used to create a wiremock response for ReviewsRestClient.
        // csll will first be made to MoviesInfoRestClient.retrieveMovieInfo via the .get in the //when section
        // ReviewsRestClient.java: the call will be transferred to the wiremock after .retrieve() in retrieveReview(String movieId) function
        stubFor(get(urlPathEqualTo("/v1/reviews/qp"))  // url path is Review url for request parm
                .willReturn(aResponse()
                        .withStatus(404)));


        //when
        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)   // url of the Movie service
                .exchange()
                .expectStatus().is4xxClientError()
                        .expectBody(String.class)
                                .isEqualTo("There is no MovieInfo available for the passed in Id : abc");

        //then
        WireMock.verify(1, getRequestedFor(urlEqualTo("/v1/movieinfos_2/" + movieId))); // // verify one request is executed for given url



    }


    @Test
    void retrieveMovieById_Reviews_404(){

        //given
        var movieId = "abc";
        // stubFor is used to create a wiremock response for MoviesInfoRestClient.
        // call will first be made to MoviesInfoRestClient.retrieveMovieInfo via the .get in the //when section
        // MoviesInfoRestClient.java: the call will be transferred to the wiremock after .retrieve() in retrieveMovieInfo(String movieId) function
        stubFor(get(urlEqualTo("/v1/movieinfos_2/" + movieId))  // url is MovieInfo url with request parm
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));  // movieinfo.json is available in resources/__files. This will be provided as a response
        // stubFor is used to create a wiremock response for ReviewsRestClient.
        // csll will first be made to MoviesInfoRestClient.retrieveMovieInfo via the .get in the //when section
        // ReviewsRestClient.java: the call will be transferred to the wiremock after .retrieve() in retrieveReview(String movieId) function
        //stubFor(get(urlPathEqualTo("/v1/reviews/qp"))  // url path is Review url for request parm
                stubFor(get(urlPathEqualTo("/v1/reviews/qp"))  // url path is Review url for request parm
                        .withQueryParam("%20movieInfoId", equalTo(movieId)) // //Pass the Query Parm. For some reason we need to append a leading space encoded as %20 as else the Query parm will not work.
                        .willReturn(aResponse()
                        .withStatus(404)));


        //when
        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)   // url of the Movie service
                .exchange()
                .expectStatus().isOk()
                .expectBody(Movie.class)  // refer MovieController.java
                .consumeWith(movieEntityExchangeResult -> {  // get response body
                            var movie = movieEntityExchangeResult.getResponseBody();
                            assert Objects.requireNonNull(movie).getReviewList().size() == 0; // Empty list will be returned due to 404 error
                            assertEquals("Batman Begins", movie.getMovieInfo().getName());
                        });

        //then
        WireMock.verify(1, getRequestedFor(urlEqualTo("/v1/movieinfos_2/" + movieId)));  // // verify one request is executed for given url



    }


    @Test
    void retrieveMovieById_5xx(){

        //given
        var movieId = "abc";
        // stubFor is used to create a wiremock response for MoviesInfoRestClient.
        // call will first be made to MoviesInfoRestClient.retrieveMovieInfo via the .get in the //when section
        // MoviesInfoRestClient.java: the call will be transferred to the wiremock after .retrieve() in retrieveMovieInfo(String movieId) function
        stubFor(get(urlEqualTo("/v1/movieinfos_2/" + movieId))  // url is MovieInfo url with request parm
                .willReturn(aResponse()
                        .withStatus(500)
                        .withBody(": Movie Info Service is Unavailable")));

        //when
        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)   // url of the Movie service
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .value(message ->{
                    assertEquals("Server Exception in MoviesInfoService: Movie Info Service is Unavailable", message);  // Note : The part before the : is auto appended via GlobalErrorHandler.handleRuntimeException()
                   //assertEquals("Retries exhausted: 3/3", message);  // Message generated when we use retryWhen(Retry.fixedDelay() in MoviesInfoRestClient.java without implementing .onRetryExhaustedThrow()
                });

        //then
        WireMock.verify(4, getRequestedFor(urlEqualTo("/v1/movieinfos_2/" + movieId))); // // 1 call + 3 retries : verify 4 requests are executed for given url



    }


    @Test
    void retrieveMovieById_reviews_5xx(){

        //given
        var movieId = "abc";
        // stubFor is used to create a wiremock response for MoviesInfoRestClient.
        // call will first be made to MoviesInfoRestClient.retrieveMovieInfo via the .get in the //when section
        // MoviesInfoRestClient.java: the call will be transferred to the wiremock after .retrieve() in retrieveMovieInfo(String movieId) function
        stubFor(get(urlEqualTo("/v1/movieinfos_2/" + movieId))  // url is MovieInfo url with request parm
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBodyFile("movieinfo.json")));  // movieinfo.json is available in resources/__files. This will be provided as a response
        // stubFor is used to create a wiremock response for ReviewsRestClient.
        // csll will first be made to MoviesInfoRestClient.retrieveMovieInfo via the .get in the //when section
        // ReviewsRestClient.java: the call will be transferred to the wiremock after .retrieve() in retrieveReview(String movieId) function
        //stubFor(get(urlPathEqualTo("/v1/reviews/qp"))  // url path is Review url for request parm
        stubFor(get(urlPathEqualTo("/v1/reviews/qp"))  // url path is Review url for request parm
                .withQueryParam("%20movieInfoId", equalTo(movieId)) // //Pass the Query Parm. For some reason we need to append a leading space encoded as %20 as else the Query parm will not work.
                .willReturn(aResponse()
                        .withStatus(500)
                                .withBody(": Review Service is Unavailable")));



        //when
        webTestClient.get()
                .uri("/v1/movies/{id}", movieId)   // url of the Movie service
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(String.class)
                .value(message ->{
                    assertEquals("Server Exception in REviews Service: Review Service is Unavailable", message);  // Note : The part before the : is auto appended via GlobalErrorHandler.handleRuntimeException()
                });

        //then
        WireMock.verify(4, getRequestedFor(urlPathMatching("/v1/reviews/qp"))); //   1 call + 3 retries : verify 4 requests are executed for given url


    }


}
