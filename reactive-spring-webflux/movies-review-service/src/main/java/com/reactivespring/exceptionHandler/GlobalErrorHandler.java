package com.reactivespring.exceptionHandler;

import com.reactivespring.exception.ReviewDataException;
import com.reactivespring.exception.ReviewNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
// 1. This class implements the ErrorWebExceptionHandler() function which we are over-riding for the handle() function
// 2. If Exception is thrown it will be caught by handle() function
public class GlobalErrorHandler implements ErrorWebExceptionHandler {

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {

        log.error("Exception Message is {}", ex.getMessage(), ex);

        DataBufferFactory dataBufferFactory =
                exchange.getResponse()  // extract response from ServerWbExchange
                .bufferFactory(); // Return a DataBufferFactory that can be used to create the body. We are overriding default message and use our message in the body

       var errorMessage =  dataBufferFactory.wrap(ex.getMessage().getBytes()); // the error message will be passed via ReviewHandler.validate using the operation "throw new ReviewDataException(errorMessage)"




        // If exception type is ReviewDataException( thrown from  ReviewHandler.validate) we will overwrite the default behaviour of the response
        if(ex instanceof ReviewDataException){
            exchange.getResponse().setStatusCode(HttpStatus.BAD_REQUEST);  // change the status from 500( default response denoting internal server error) to Bad Request
            return exchange.getResponse().writeWith(Mono.just(errorMessage)); // Build response body using the error message from ReviewHandler.validate thrown by "throw new ReviewDataException(errorMessage)"
        }

        // If exception type is ReviewNotFoundException( thrown from  ReviewHandler.updateReview) we will overwrite the default behaviour of the response
        if(ex instanceof ReviewNotFoundException){
            exchange.getResponse().setStatusCode(HttpStatus.NOT_FOUND);  // change the status from 500( default response denoting internal server error) to Not FOund
            return exchange.getResponse().writeWith(Mono.just(errorMessage)); // Build response body using the error message from ReviewHandler.validate thrown by "throw new ReviewDataException(errorMessage)"
        }

        // in case of any other errors return Internal Server Error
        exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return exchange.getResponse().writeWith(Mono.just(errorMessage)); // Build response body using the error message from ReviewHandler.validate thrown by "throw new ReviewDataException(errorMessage)"
    }
}
