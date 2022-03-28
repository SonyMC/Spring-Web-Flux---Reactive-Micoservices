package com.reactivespring.handler;

import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import com.reactivespring.repository.ReviewReactiveRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Sinks;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.stream.Collectors;


@Component
@Slf4j
public class ReviewHandler  {

    @Autowired
    private ReviewReactiveRepository reviewReactiveRepository;

    // Note : Validation annotations @Valid ( refer MoviesInfoController.updateMovieInfo) cannot be used in functional web
    // We have to explicitly use the validator instead of using annotations in functional web
    @Autowired
    private Validator validator;


    //Publish & Subscribe : Sink publishes multiple events and will replay all events once a subscriber is connected to it
    Sinks.Many<Review> reviewsSink = Sinks.many().replay().all();
    //Sinks.Many<MovieInfo> moviesInfoSink = Sinks.many().replay().latest(); //// replay().latest() -> First subscriber will get all and subsequently only the latest event will be streamed to all subscriber.


    public Mono<ServerResponse> addReview(ServerRequest request) {
        // Now map the Mono returned above as a Server Response
        return request.bodyToMono(Review.class)       // Extract request body containing Review object as Mono
                // call validation after extracting the class
                // doOnNext is a side effect function which can perform validations on the extracted class
                // we will apply bean validation and if any validation fails, we will break teh flow and throw an exception
                .doOnNext(this::validate)
               .flatMap(reviewReactiveRepository::save)   // we are doing a save operation on teh Mono above and returning the value. Save is a reactive operation and so we have to use flatMap operator
               .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);  // map the Mono returned above to a Server Response

    }


    //will provide a set of constraint violations for Review domain
    private void validate(Review review) {

       var constraintViolations =  validator.validate(review);
       // log the constraint violations
       log.info("constraintViolations: {} ", constraintViolations);

       // If there are violation messages , then Build an error message using extracted violations
       if(constraintViolations.size() <= 0)   // Note: constraintViolations is a Set
           return;

       var errorMessage =  constraintViolations
                .stream()  // Note: constraintViolations is a Set
                .map(ConstraintViolation::getMessage)  // get the violation messages
                .sorted() // sort th emessage
                .collect(Collectors.joining(","));  // collect the messages into a String and separate them using a coma

       // throw exception
        throw new ReviewDataException(errorMessage);
    }

    public Mono<ServerResponse> addReview_2(ServerRequest request) {
        // Now map the Mono returned above as a Server Response
        var reviewToBeSaved = request.bodyToMono(Review.class);

        return ServerResponse.ok().body(reviewToBeSaved,Review.class);

    }


    public Mono<ServerResponse> getReview(ServerRequest request) {

         var reviewsFlux = reviewReactiveRepository.findAll();
         return ServerResponse.ok().body(reviewsFlux, Review.class);    //body returns Mono
    }

    public Mono<ServerResponse> getReviewbyMovieId_QueryParm(ServerRequest request) {

        // First extract the movieInfoID provided in the query parm withthe request
        // Note: For some weird reason we have to place a space or any character in front of the query parameter while making teh rest call !!! So instead of 'movieInfoId' use ' movieInfoId' as quey key.
        var movieInfoId= request.queryParam("movieInfoId");

        //queryParam() function above returns an optional of String, enabling to check whether the value is present or not
        if(movieInfoId.isPresent()){
            //var reviewsFlux = reviewReactiveRepository.findReviewsByMovieInfoId(Long.valueOf(movieInfoId.get()));  // extract long value from string
            var reviewsFlux  = reviewReactiveRepository.findReviewsByMovieInfoId(String.valueOf(movieInfoId));
            return buildServerResponseMono(reviewsFlux);
        }else{  // if the movieInfo for the supplied info is not found, then get all reviews
            var reviewsFlux = reviewReactiveRepository.findAll();
            return buildServerResponseMono(reviewsFlux);


        }

    }

    private Mono<ServerResponse> buildServerResponseMono(Flux<Review> reviewsFlux) {
        return ServerResponse.ok().body(reviewsFlux, Review.class);  //body returns Mono
    }

    public Mono<ServerResponse> updateReview(ServerRequest request) {
        // First extract the path variable for review id from request
        var reviewId= request.pathVariable("id");

        // Now check if the given id is available in the DB or not by extracting the review for the id
        var existingReview = reviewReactiveRepository.findById(reviewId)
                                                     .switchIfEmpty(Mono.error(new ReviewNotFoundException("Review not found for given Review ID" + reviewId)));

        // Now access the review as passed in the request and update the values of the extracted review with it
        return existingReview
                // flat map operation to return updated Review with values from the review passed in the request
                .flatMap(review -> request.bodyToMono(Review.class)  // access the review passed in the request which contains the values to be updated
                .map(reqReview -> {
                    review.setComment(reqReview.getComment());   //now map the extracted review with updated values passed in the request
                    review.setRating(reqReview.getRating());
                    review.setMovieInfoId(reqReview.getMovieInfoId());
                    return review;
                })
                        // save the updated review and pass as a server response
                        .flatMap(reviewReactiveRepository::save)  // save the updated values
                        .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview)) // map as a server response
             );
    }


    public Mono<ServerResponse> updateReview_Option2(ServerRequest request) {
        // First extract the path variable for review id from request
        var reviewId= request.pathVariable("id");

        // Now check if the given id is available in the DB or not by extracting the review for the id
        var existingReview = reviewReactiveRepository.findById(reviewId);

        // Now access the review as passed in the request and update the values of the extracted review with it
        return existingReview
                // flat map operation to return updated Review with values from the review passed in the request
                .flatMap(review -> request.bodyToMono(Review.class)  // access the review passed in the request which contains the values to be updated
                        .map(reqReview -> {
                            review.setComment(reqReview.getComment());   //now map the extracted review with updated values passed in the request
                            review.setRating(reqReview.getRating());
                            review.setMovieInfoId(reqReview.getMovieInfoId());
                            return review;
                        })
                        // save the updated review and pass as a server response
                        .flatMap(reviewReactiveRepository::save)  // save the updated values
                        .flatMap(savedReview -> ServerResponse.ok().bodyValue(savedReview))) // map as a server response
                .switchIfEmpty(ServerResponse.notFound().build());

    }

    public Mono<ServerResponse> deleteReview(ServerRequest request) {

        // First extract the path variable for review id from request
        var reviewId = request.pathVariable("id");

        // Now check if the given id is available in the DB or not by extracting the review for the id
        var existingReview = reviewReactiveRepository.findById(reviewId);

        // Now access the review as passed in the request and delete from repo
        return existingReview
                // delete review and return no content response.
                .flatMap(review -> reviewReactiveRepository.deleteById(reviewId))  // delete teh review by id. Return a Mono<vvoid
                .then(ServerResponse.noContent().build());  // Since delte will return a Mono<void> which is nothing, we will substitiute wit a no Content Response


        }


     public Mono<ServerResponse> addReviewPublishStream(ServerRequest request) {
            // Now map the Mono returned above as a Server Response
            return request.bodyToMono(Review.class)       // Extract request body containing Review object as Mono
                    // call validation after extracting the class
                    // doOnNext is a side effect function which can perform validations on the extracted class
                    // we will apply bean validation and if any validation fails, we will break teh flow and throw an exception
                    .doOnNext(this::validate)
                    .flatMap(reviewReactiveRepository::save)   // we are doing a save operation on the Mono above and returning the value. Save is a reactive operation and so we have to use flatMap operator
                    // 1. Access the saved review using donOnNext() Operator
                    // 2. Publish the review : Manual triggering using tryEmitNext()
                    // 3. tryEventNext() automatically takes care of the Failure Handler and so no explicit FailureHandler is required
                    .doOnNext(reviewsSink::tryEmitNext)
//                    .doOnNext(savedReview -> {
//                        reviewsSink.tryEmitComplete();   // will stop publishing as soon as the first review is published
//                    })
                    /*
                    ** Equivalent Lambda representation of doOnNext().tryEmitNext()
                     */
                    //.doOnNext(savedReview -> {
                    //    reviewsSink.tryEmitNext(savedReview);
                    //})
                    .flatMap(ServerResponse.status(HttpStatus.CREATED)::bodyValue);  // map the Mono returned above to a Server Response

        }


    // Subscribe to the above published SSE
    // Client will connect to the url which will automatically subscribe to that particular sink which will continuously publish the movie info events
    // On hittign this url, it will keep on subscribing to the published events. On a browser this will keep on downloading the json file. In order to see a result uncomment 'moviesInfoSink.tryEmitComplete()' operator in addMovieInfo_publish() above. This will donload only one event.


    public Mono<ServerResponse> getReviewSubscribeStream(ServerRequest request) {

        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)   // the response can be viewed in browser is we uncomment tryEmitComplete() block in addReviewPublishStream() above. Else it will keep on subscribing in a loop
                .body(reviewsSink.asFlux(), Review.class)
                .log();
    }
}


