package com.reactivespring.controller;


import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import com.reactivespring.service.MoviesInfoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.when;


@WebFluxTest(controllers = MoviesInfoController.class)   // pass teh controller we are going to unit test
@AutoConfigureWebTestClient
public class MoviesInfoControllerUnitTest {

    @Autowired
    private WebTestClient webTestClient;


    //@Autowired
    //MovieInfoRepository movieInfoRepository;

    static String MOVIES_INFO_URL = "/v1/movieinfos";
    static String MOVIES_INFO_URL_ID = "/v1/movieinfos/abc";
    static String MOVIES_INO_URL_YEAR = "/v1/movieinfosForYear";
    static String MOVIES_INO_URL_ID_APPROACH2 = "/v1/movieinfos_2/abc";
    static String MOVIES_INO_URL_ID_APPROACH2_SANS_ID = "/v1/movieinfos_2";





/*
- MoviesInforController.java has a dependency on MoviesInfoService( defined using 'private MoviesInfoService moviesInfoService')
- We are going to mock MoviesInfoService

 */

    @MockBean
    private MoviesInfoService moviesInfoServiceMock;

    @Test
    void getAllMoviesInfo(){

        //given
        var movieinfos = List.of(new MovieInfo(null, "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15")),
                new MovieInfo(null, "The Dark Knight",
                        2008, List.of("Christian Bale", "HeathLedger"), LocalDate.parse("2008-07-18")),
                new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20")));

        //when
        // mock movieInfoService method for getting all movies which returns a flux.
        // Note : here we will not be interacting with the Mongo DB
        when(moviesInfoServiceMock.getAllMovieInfos()).thenReturn(Flux.fromIterable(movieinfos));


        //then
        webTestClient
                .get()
                .uri(MOVIES_INFO_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(MovieInfo.class)  // we are expecting Body List since a Flux will eb returned
                .hasSize(3);  // we are adding 3 movies at beginning

    }

    @Test
    void getMovieInfoByID() {

        //given
        var id = "abc";


        //when
        when(moviesInfoServiceMock.getMovieInfoByID(isA(String.class)))
                .thenReturn(Mono.just(new MovieInfo("abc", "Dark Knight Rises",
                        2012, List.of("Christian Bale", "Tom Hardy"), LocalDate.parse("2012-07-20"))));

        //then
        webTestClient
                .get()
                // .uri(MOVIES_INFO_URL_ID)
                .uri(MOVIES_INFO_URL + "/{id}", id)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody()
                .jsonPath("$.name").isEqualTo("Dark Knight Rises");

    }


    @Test
    void addMovieInfo() {

        // given
        var movieInfo = new MovieInfo(null, "Batman Begins",
                2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"));

        //when
        when(moviesInfoServiceMock.addMovieInfo(isA(MovieInfo.class)))
                .thenReturn(Mono.just(new MovieInfo("mockID", "Batman Begins",
                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"))));

        //then
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


    }



    @Test
    void addNewMovieInfo_validation() {


        // given
        var movieInfo = new MovieInfo(null, "",
                -2005, List.of(""), LocalDate.parse("2005-06-15"));

       //when
//        when(moviesInfoServiceMock.addMovieInfo(isA(MovieInfo.class)))
//                .thenReturn(Mono.just(new MovieInfo("mockID", "Batman Begins",
//                        2005, List.of("Christian Bale", "Michael Cane"), LocalDate.parse("2005-06-15"))));

        //then
        webTestClient
                .post()
                .uri(MOVIES_INFO_URL)
                .bodyValue(movieInfo)
                .exchange()  //make the call to the end point
                .expectStatus()   // which status is expected?
                .isBadRequest()
                .expectBody(String.class)
                .consumeWith(result -> {
                        var error = result.getResponseBody();
                        assert error != null;
                        String expectedErrorMessage = "movieInfo.cat must be present,movieInfo.name must be present,movieInfo.year must be a positive value";
                        assertEquals(expectedErrorMessage, error);
                });

    }


    @Test
    void updateMovieInfo() {

        //given
        var id = "abc";
        var updateMovieInfo = new MovieInfo("abc", "Dark Knight Rises 1",
                2013, List.of("Christian Bale1", "Tom Hardy1"), LocalDate.parse("2012-07-20"));


        //when
        when(moviesInfoServiceMock.updateMovieInfo(isA(MovieInfo.class), isA(String.class)))
                .thenReturn(Mono.just(updateMovieInfo));

        //then
        webTestClient
                .put()
                .uri(MOVIES_INFO_URL + "/{id}", id)
                .bodyValue(updateMovieInfo)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(MovieInfo.class)
                .consumeWith(movieInfoEntityExchangeResult -> {
                    var movieInfo = movieInfoEntityExchangeResult.getResponseBody();
                    assert movieInfo != null;          // assert updated vale is not null
                    assertEquals("Dark Knight Rises 1", movieInfo.getName());

                });
    }

    @Test
    void updateMovieInfo_NotFound() {

        //given
        var id = "abc1";
        var updateMovieInfo = new MovieInfo("abc", "Dark Knight Rises 1",
                2013, List.of("Christian Bale1", "Tom Hardy1"), LocalDate.parse("2012-07-20"));


        //when
        when(moviesInfoServiceMock.updateMovieInfo(isA(MovieInfo.class), isA(String.class)))
                .thenReturn(Mono.empty());

        //then
        webTestClient
                .put()
                .uri(MOVIES_INFO_URL + "/{id}", id)
                .bodyValue(updateMovieInfo)
                .exchange()
                .expectStatus()
                .isNotFound();

    }


    @Test
    void deleteMovieInfoById(){

        //given
        var id = "abc";

        //when
        when(moviesInfoServiceMock.deleteMovieInfoById(isA(String.class)))
                .thenReturn(Mono.empty());

        //then
            webTestClient
                .delete()
                .uri(MOVIES_INFO_URL + "/{id}", id)
                .exchange()
                .expectStatus()
                .isNoContent();
    }
}
