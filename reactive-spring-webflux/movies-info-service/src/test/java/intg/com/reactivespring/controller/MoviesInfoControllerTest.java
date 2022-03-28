package com.reactivespring.controller;

import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;



@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)   // will spin up Spring app in a random port
@ActiveProfiles("test")   // Ensure this is different to all other profiles provided in  application.yml
@AutoConfigureWebTestClient
@TestPropertySource(properties = "spring.mongodb.embedded.version=3.5.5")   // need to include this else test case wil fail
class MoviesInfoControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    MovieInfoRepository movieInfoRepository;

    static String MOVIES_INFO_URL = "/v1/movieinfos";
    static String MOVIES_INFO_URL_ID = "/v1/movieinfos/abc";
    static String MOVIES_INO_URL_YEAR = "/v1/movieinfosForYear";
    static String MOVIES_INO_URL_ID_APPROACH2 = "/v1/movieinfos_2/abc";
    static String MOVIES_INO_URL_ID_APPROACH2_SANS_ID = "/v1/movieinfos_2";
    static String MOVIES_INO_URL_NAME = "/v1/movieinfosForName";
    static String MOVIES_INFO_PUBLISHER = "/v1/movieinfos_publish";
    static String MOVIES_INFO_SUBSCRIBER = "/v1/movieinfos/stream";

    // will execute this method before each and every test case
    // load data into the movieInfo repository
    // Note: 'id' which is teh primary key will be auto generated in Mongo DB aif not provided. Else it will take teh supplied value.
    @BeforeEach
    void setUp() {
        var movieinfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));


        movieInfoRepository.saveAll(movieinfos)
                .blockLast();  // blockLast() ensures this method is completed before the other methods begin execution.This is because we are using asychronous non-blocking calls
        // Note: Such blocking calls can only be used in testcases as else SpringWebFlux will throw runtime exceptions
/*

        movieInfoRepository
                .deleteAll()
                .thenMany(movieInfoRepository.saveAll(movieinfos))
                .blockLast();// blockLast() ensures this method is completed before the other methods begin execution.This is because we are using asychronous non-blocking calls
        // Note: Such blocking calls can only be used in testcases as else SpringWebFlux will throw runtime exceptions


 */


    }


    // will execute after all test-cases have been executed
    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block(); // execute a blocking call to delete all data in the Mongo repo after execution of test cases.

    }

    @Test
    void addMovieInfo() {

        //given
        var movieInfo = new MovieInfo(null, "Batman Forever",
                2005, List.of("Michael Peaton", "Michael Cane"), LocalDate.parse("2005-06-15"));

        //when

        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()  //make the call to the end point
                .expectStatus()   // which status is expected?
                .isCreated()   // Expected status is Created
                .expectBody(MovieInfo.class)   // Expected Body is an object of MovieInfo domain
                .consumeWith(movieInfoEntityExchangeResult -> {  // Extract the result and assert it is not null
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedMovieInfo != null;
                    assert savedMovieInfo.getMovieInfoID() != null;
                });

        //then
    }

    @Test
    void getAllMovieInfos() {


        //given

        //when

        webTestClient
                .get()
                .uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)  // we are expecting Body List since a Flux will eb returned
                .hasSize(3);  // we are adding 3 movies at beginning


        //then
    }

    @Test
    void getMovieInfoById() {

        //given
        var id = "abc";
        //when

        webTestClient
                .get()
               // .uri(MOVIES_INFO_URL_ID)
                .uri(MOVIES_INFO_URL + "/{id}", id)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)  // returns a Mono
                .consumeWith(movieInfoEntityExchangeResult -> {  // Extract the result and assert it is not null
                    var movieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert movieInfo != null;
                    assert movieInfo.getMovieInfoID().equals("abc");
                });


        //then

    }

    @Test
    void getMovieInfoById_JSON() {

        //given
        var id = "abc";
        //when

        webTestClient
                .get()
                // .uri(MOVIES_INFO_URL_ID)
                .uri(MOVIES_INFO_URL + "/{id}", id)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()  // No class specified
                .jsonPath("$.name") // we are specifying teh json variable we are interested in
                .isEqualTo("Dark Knight Rises");

        //then

    }


    @Test
    void getMovieInfoById_approach2() {

        //given

        //when



        //then
        webTestClient
                .get()
                .uri(MOVIES_INO_URL_ID_APPROACH2)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()  // No class specified
                .jsonPath("$.name") // we are specifying the json variable we are interested in
                .isEqualTo("Dark Knight Rises");

    }

    @Test
    void getMovieInfoById_NotFound() {

        //given
        var id = "def";
        //when


        //then
        webTestClient
                .get()
                .uri(MOVIES_INO_URL_ID_APPROACH2_SANS_ID + "/{id}", id)
                .exchange()
                .expectStatus()
                .isNotFound();

    }

    @Test
    void getAllMovieInfosForYear() {

        //given
        Integer inputYear = 2008;

        // Build url with query parm year=2008
        var uri = UriComponentsBuilder.fromUriString(MOVIES_INO_URL_YEAR)
                        .queryParam("year",inputYear)
                .buildAndExpand().toUri();

        //when


        //then
        webTestClient
                .get()
                .uri(uri)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)
                .hasSize(1);




    }

    @Test
    void getAllMovieInfosForYear_testApproach2() {

//        //given
        Integer inputYear = 2008;


        //when


        //then
        webTestClient
                .get()
                .uri(uriBuilder -> {   // Build uri
                    return uriBuilder
                            .path(MOVIES_INO_URL_YEAR)
                            .queryParam("year", inputYear)
                            .build();
                        })
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)  // we are expecting a Body List since a Flux will eb returned
                .hasSize(1);  // one movie in year 2008


    }


    @Test
    void getAllMovieInfoForName() {

        //given
        String name = "Batman Begins" ;

        //when

        webTestClient
                .get()
                .uri(MOVIES_INO_URL_NAME + "/{name}", name)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()  // No class specified
                .jsonPath("$.name") // we are specifying teh json variable we are interested in
                .isEqualTo("Batman Begins");

    }


    @Test
    void updateMovieInfo() {

        //given
        var id = "abc";
        var updatedMovieInfo = new MovieInfo("abc","Gotham",2021,List.of("Unknown","Legend"),LocalDate.parse("2022-02-11"));

        //when

        //then
        webTestClient
                .put()
                .uri(MOVIES_INFO_URL + "/{id}",id)
                .bodyValue(updatedMovieInfo)  // pass updated movie info
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var movieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert movieInfo != null;          // assert updated vale is not null
                    assertEquals("Gotham", movieInfo.getName());
                });


    }



    @Test
    void updateMovieInfo_NotFound() {

        //given
        var id = "def";
        var updatedMovieInfo = new MovieInfo("xxx","Gotham",2021,List.of("Unknown","Legend"),LocalDate.parse("2022-02-11"));

        //when

        //then
        webTestClient
                .put()
                .uri(MOVIES_INO_URL_ID_APPROACH2_SANS_ID + "/{id}",id)
                .bodyValue(updatedMovieInfo)  // pass updated movie info
                .exchange()
                .expectStatus()
                .isNotFound();
    }


    @Test
    void deleteMovieInfoById() {

        //given
        var id = "abc";

        //when

        //then
        webTestClient
                .delete()
                .uri(MOVIES_INFO_URL + "/{id}", id)
                .exchange()
                .expectStatus()
                .isNoContent();
    }


    @Test
    void getAllMovieInfos_stream() {

        /*
        *** 1. Create and Publish the Movie Info  Stream
         */

        //given
        var movieInfo = new MovieInfo(null, "Batman Forever",
                2005, List.of("Michael Peaton", "Michael Cane"), LocalDate.parse("2005-06-15"));

        //when

        webTestClient
                .post()
                .uri(MOVIES_INFO_PUBLISHER)
                .bodyValue(movieInfo)
                .exchange()  //make the call to the end point
                .expectStatus()   // which status is expected?
                .isCreated()   // Expected status is Created
                .expectBody(MovieInfo.class)   // Expected Body is an object of MovieInfo domain
                .consumeWith(movieInfoEntityExchangeResult -> {  // Extract the result and assert it is not null
                    var savedMovieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert savedMovieInfo != null;
                    assert savedMovieInfo.getMovieInfoID() != null;
                });


        /*
        ** 2. Subscribe to the Movie Info Stream
         */
        //then

        var moviesStreamFlux =
                webTestClient
                .get()
                .uri(MOVIES_INFO_SUBSCRIBER)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(MovieInfo.class)
                        .getResponseBody();

        StepVerifier.create(moviesStreamFlux)
                .assertNext(movieInfo1 -> {
                    assert movieInfo1.getMovieInfoID() != null;
                })
                .thenCancel()  // we do not want the subscription to run indefinitely.
                .verify();

    }

}