package com.reactivespring.routes;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exceptionHandler.GlobalErrorHandler;
import com.reactivespring.handler.ReviewHandler;
import com.reactivespring.repository.ReviewReactiveRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {ReviewRouter.class, ReviewHandler.class, GlobalErrorHandler.class})                 // get beans automatically
@AutoConfigureWebTestClient
public class ReviewsUnitTest {


    @MockBean
    private ReviewReactiveRepository reviewReactiveRepositoryMock;


    @Autowired
    private WebTestClient webTestClient;

    static String REVIEWS_URL = "/v1/reviews";






    @Test
    void getAllReviews(){

        //given
        var reviewsList = List.of(
                new Review(null, "1", "Awesome Movie", 9.0),
                new Review(null, "1", "Awesome Movie1", 9.0),
                new Review("abc", "1", "Excellent Movie", 8.0));

        //when
        when(reviewReactiveRepositoryMock.findAll()).thenReturn(Flux.fromIterable(reviewsList));


        //then
        webTestClient
                .get()
                .uri(REVIEWS_URL)
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Review.class)  // we are expecting Body List since a Flux will eb returned
                .hasSize(3);  // we are adding 3 movies at beginning

    }


    @Test
    void addReview(){

        var review = new Review(null, "1", "Awesome Movie", 9.0);

        //when
        // mock reviewReactiveRepositoryMock method for posing a review which returns a mono.
        // Note : here we will not be interacting with the Mongo DB
        when(reviewReactiveRepositoryMock.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("xxx", "1", "Awesome Movie", 9.0)));


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

   // Test for validation of Review.java
    @Test
    void addReview_validation(){

        var review = new Review(null, null, "Awesome Movie", -9.0);

        //when
        // mock reviewReactiveRepositoryMock method for posing a review which returns a mono.
        // Note : here we will not be interacting with the Mongo DB
        when(reviewReactiveRepositoryMock.save(isA(Review.class)))
                .thenReturn(Mono.just(new Review("xxx", "1", "Awesome Movie", 9.0)));


        // then
        webTestClient
                .post()
                .uri(REVIEWS_URL)
                .bodyValue(review)
                .exchange()  //make the call to the end point
                .expectStatus()   // which status is expected?
                .isBadRequest()
                .expectBody(String.class)
                .isEqualTo("rating.movideInfoIdD : cannot be null ,rating.negative : please pass a non-negative value");


    }



    @Test
    void getReviewbyMovieId_QueryParm() {

        //given



        //when
        when(reviewReactiveRepositoryMock.findReviewsByMovieInfoId(isA(String.class)))
                .thenReturn(Flux.just(new Review("xxx", "1", "Awesome Movie", 9.0)));
        //given
        String queryParm_movieInfoId = "1";

        var reviewsList = List.of(
                new Review(null, "1", "Awesome Movie", 9.0),
                new Review(null, "1", "Awesome Movie1", 9.0));

        //when
        when(reviewReactiveRepositoryMock.findReviewsByMovieInfoId(isA(String.class)))
                .thenReturn(Flux.fromIterable(reviewsList));

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


    @Test
    void updateReview() {
        //given

        var reviewUpdate = new Review(null, "1", "Not an Awesome Movie", 8.0);


        //when
        // save the move
        when(reviewReactiveRepositoryMock.save(isA(Review.class))).thenReturn(Mono.just(new Review("abc", "1", "Not an Awesome Movie", 8.0)));
        // update the movie
        when(reviewReactiveRepositoryMock.findById((String) any())).thenReturn(Mono.just(new Review("abc", "1", "Awesome Movie", 9.0)));

        //the
        webTestClient
                .put()
                .uri("/v1/reviews/{id}", "abc")
                .bodyValue(reviewUpdate)
                .exchange()
                .expectStatus().isOk()
                .expectBody(Review.class)
                .consumeWith(reviewResponse -> {
                    var updatedReview = reviewResponse.getResponseBody();
                    assert updatedReview != null;
                    System.out.println("updatedReview : " + updatedReview);
                    assertEquals(8.0, updatedReview.getRating());
                    assertEquals("Not an Awesome Movie", updatedReview.getComment());
                });
    }



    @Test
    void deleteReview() {

        //given
        var reviewId= "abc";

        //when
        when(reviewReactiveRepositoryMock.findById((String) any())).thenReturn(Mono.just(new Review("abc", "1", "Awesome Movie", 9.0)));
        when(reviewReactiveRepositoryMock.deleteById((String) any())).thenReturn(Mono.empty());

        //then
        webTestClient
                    .delete()
                    .uri("/v1/reviews/{id}", reviewId)
                    .exchange()
                    .expectStatus().isNoContent();
        }




}
