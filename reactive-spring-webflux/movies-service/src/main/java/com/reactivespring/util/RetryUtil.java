package com.reactivespring.util;

import com.reactivespring.exception.MoviesInfoServerException;
import com.reactivespring.exception.ReviewsServerException;
import reactor.core.Exceptions;
import reactor.util.retry.Retry;
import reactor.util.retry.RetrySpec;

import java.time.Duration;

public class RetryUtil {

    public static Retry retrySpec(){
       return  RetrySpec.fixedDelay(3, Duration.ofSeconds(1))  // max retry attempts of 3 with an interval of 1 second
                //Set the Predicate that will filter which errors can be retried
                .filter((ex) -> ex instanceof MoviesInfoServerException || ex instanceof ReviewsServerException)  // retry only for exceptions which belong to MoviesInfoServerException OR ReviewsServerException
                //Set the generator for the Exception to be propagated when the maximum amount of retries is exhausted
                .onRetryExhaustedThrow((((retryBackoffSpec, retrySignal) -> Exceptions.propagate(retrySignal.failure()))));  // propagate root cause of issue to client
    }
}
