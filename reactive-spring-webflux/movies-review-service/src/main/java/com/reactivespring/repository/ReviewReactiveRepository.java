package com.reactivespring.repository;

import com.reactivespring.domain.Review;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;

import java.util.function.LongFunction;

// We will be using the type Review and String( as id is defined a String in Review.java) as parms
public interface ReviewReactiveRepository extends ReactiveMongoRepository<Review, String> {

    // custom query
    Flux<Review> findReviewsByMovieInfoId(String movieInfoId);





}
