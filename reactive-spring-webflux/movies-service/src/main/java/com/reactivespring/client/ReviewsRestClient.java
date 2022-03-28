package com.reactivespring.client;


import com.reactivespring.domain.MovieInfo;
import com.reactivespring.domain.Review;
import com.reactivespring.exception.ReviewsClientException;
import com.reactivespring.exception.ReviewsServerException;
import com.reactivespring.util.RetryUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.util.UriComponentsBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
@Slf4j
public class ReviewsRestClient {

    private WebClient webClient;

    @Value("${restClient.reviewsUrl}")   // key-value pair is specified in application.yaml
    private String reviewsUrl;  // variable will contain value as specified in application.yaml

    // auto inject web client via constructor
    public ReviewsRestClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Flux<Review> retrieveReview(String movieId){


        // Build url to include query parameter  for "movieInfoId"
        var url = UriComponentsBuilder.fromHttpUrl(reviewsUrl)
                // ** Note : The query parm "movieInfoId" needs a space or some rando char in front else which the rest service will not works
                .queryParam(" movieInfoId", movieId)   // build url by mapping provided id to movieInfoID which will be taken as a query parm
                .buildAndExpand()
                .toString();

        return webClient.get()
                .uri(url)
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, (clientResponse -> {
                    log.info("Status code : {}", clientResponse.statusCode().value());
                    if(clientResponse.statusCode().equals(HttpStatus.NOT_FOUND)){
                        return Mono.empty();
                    }
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(response -> Mono.error(new ReviewsClientException(response)));
                }))
                .onStatus(HttpStatus::is5xxServerError, (clientResponse -> {
                    log.info("Status code : {}", clientResponse.statusCode().value());
                    return clientResponse.bodyToMono(String.class)
                            .flatMap(response -> Mono.error(new ReviewsServerException("Server Exception in REviews Service" + response)));
                }))
                .bodyToFlux(Review.class)
                .retryWhen(RetryUtil.retrySpec())
                .log();


    }



}
