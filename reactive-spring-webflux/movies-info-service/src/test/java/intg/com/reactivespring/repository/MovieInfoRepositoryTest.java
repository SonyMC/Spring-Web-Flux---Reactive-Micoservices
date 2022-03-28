package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataMongoTest  // scans application and looks for Repository classes and make that Repository class available for your testing
@TestPropertySource(properties = "spring.mongodb.embedded.version=3.5.5")   // need to include this else test case wil fail
@ActiveProfiles("test")  // needed in order to use teh embedded mongodb
class MovieInfoRepositoryTest {

    @Autowired
    MovieInfoRepository movieInfoRepository;


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
    }


   // will execute after all test-cases have been executed
    @AfterEach
    void tearDown() {
        movieInfoRepository.deleteAll().block(); // execute a blocling call to delete all data in the Mongo repo after execution of test cases.

    }

    @Test
    void findall() {

        //given

        //when
        var moviesInfoFlux = movieInfoRepository.findAll().log();

        StepVerifier.create(moviesInfoFlux)
                .expectNextCount(3)
                .verifyComplete();
    

        //then

    }

    @Test
    void findbyID() {

        //given

        //when
        var moviesInfoMono = movieInfoRepository.findById("abc").log();

        StepVerifier.create(moviesInfoMono)
                .assertNext(movieInfo1 -> {
                    assertEquals("Dark Knight Rises",movieInfo1.getName());
                });

        //then

    }

    @Test
    void saveMovieInfo() {

        //given
        var movieInfo = new MovieInfo(null,"The Batman",2022,List.of("Rober Pattinson","Matt Reeves"),LocalDate.parse("2022-03-27"));

        //when
        var savedMovieInfoMono = movieInfoRepository.save(movieInfo).log();


        //then

        StepVerifier.create(savedMovieInfoMono)
                .assertNext(movieInfo1 -> {
                    assertNotNull(movieInfo1.getMovieInfoID());
                    assertEquals("The Batman",movieInfo1.getName());;
                });

    }

    @Test
    void updateMovieInfo() {

        //given
        var movieInfo = movieInfoRepository.findById("abc").block();
        movieInfo.setYear(2022);  // update the year

        //when
        var savedMovieInfoMono = movieInfoRepository.save(movieInfo); // save the update into the repo



        //then

        StepVerifier.create(savedMovieInfoMono)
                .assertNext(movieInfo1 -> {
                    assertNotNull(movieInfo1.getMovieInfoID());
                    assertEquals(2022,movieInfo1.getYear());
                });

    }


    @Test
    void deleteMovieInfo() {

        //given

        //when
        movieInfoRepository.deleteById("abc").block();
        var movieInfoFlux = movieInfoRepository.findAll();


        //then

        StepVerifier.create(movieInfoFlux)
                .expectNextCount(2)    // out of 3 recs, we have deleted one
                .verifyComplete();


    }

}