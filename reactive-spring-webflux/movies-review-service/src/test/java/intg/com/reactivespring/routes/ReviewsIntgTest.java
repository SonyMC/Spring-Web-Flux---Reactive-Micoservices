package com.reactivespring.routes;


import com.reactivespring.domain.Review;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)   // will spin up Spring app in a random port
@ActiveProfiles("test")   // Ensure this is different to all other profiles provided in  application.yml
@AutoConfigureWebTestClient
@TestPropertySource(properties = "spring.mongodb.embedded.version=3.5.5")   // need to include this else test case wil fail
public class  ReviewsIntgTest {

    @Autowired
    WebTestClient webTestClient;

    @Autowired
    ReviewReactiveRepository reviewReactiveRepository;

    static String REVIEWS_URL = "/v1/reviews";


    @BeforeEach
    void setUp() {

        var reviewsList = List.of(
                new Review(null, "1", "Awesome Movie", 9.0),
                new Review(null, "1", "Awesome Movie1", 9.0),
                new Review("abc", "2", "Excellent Movie", 8.0));

        reviewReactiveRepository.saveAll(reviewsList)
                .blockLast();

    }


    @AfterEach
    void tearDown() {
        reviewReactiveRepository.deleteAll().block(); // delete all data once tests hav ebeen performed

    }

    @Test
    void addReview(){

        // given
        var review = new Review(null, "1", "Awesome Movie", 9.0);

        // when

        // then
        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()  //make the call to the end point
                .expectStatus()   // which status is expected?
                .isCreated()   // Expected status is Created
                .expectBody(Review.class)   // Expected Body is an object of MovieInfo domain
                .consumeWith(movieReviewEntityExchangeResult -> {  // Extract the result and assert it is not null
                    var savedReview = movieReviewEntityExchangeResult .getResponseBody();
                    assert savedReview != null;
                    assert savedReview.getReviewId() != null;
                });


    }


    @Test
    void getReview(){


        //given

        //when

        //then
        webTestClient
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)  // we are expecting Body List
                .hasSize(3);  // we are adding 3 movies at beginning


    }


    @Test
    void updateReview() {

        //given
        var reviewId = "abc";
        var updatedReview = new Review("abc", "2", "Run Baby Run", 8.0);

        //when

        //then
        webTestClient
                .put()
                .uri(REVIEWS_URL + "/{id}",reviewId)
                .bodyValue(updatedReview)  // pass updated movie info
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(Review.class)
                .consumeWith(movieReviewEntityExchangeResult -> {
                    var movieReview = movieReviewEntityExchangeResult.getResponseBody();
                    assert  movieReview != null;          // assert updated vale is not null
                    assertEquals("Run Baby Run", movieReview.getComment());
                });


    }


    @Test
    void deleteReview() {

          //given
        var reviewId = "abc";

        //when

        //then
        webTestClient
                .delete()
                .uri(REVIEWS_URL + "/{id}",reviewId)
                .exchange()
                .expectStatus()
                .isNoContent();
    }


    @Test
    void getReviewByMovieInfoById()

    {

        //given
        String queryParm_movieInfoId = "1";


        //when


        //then
        webTestClient
                .get()
                .uri(uriBuilder -> {   // Build uri
                    return uriBuilder
                            .path(REVIEWS_URL + "/qp")
                            .queryParam("movieInfoId", queryParm_movieInfoId)
                            .build();
                })
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)
                .value(reviewList -> {
                    System.out.println("reviewList : " + reviewList);
                    assertEquals(2, reviewList.size());

                });

    }

}
