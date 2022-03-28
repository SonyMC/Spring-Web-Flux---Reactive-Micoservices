package com.reactivespring.exceptionHandler;

import com.reactivespring.exception.MovieInfoNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.stream.Collectors;

@ControllerAdvice
@Slf4j
public class GlobalErrorHandler {


    @ExceptionHandler(WebExchangeBindException.class)  // Bind exception is thrown for Bean validations
    public ResponseEntity<String> handleRequestBodyError(WebExchangeBindException ex){
        log.error("Exception caught in handleRequestBodyError : {}", ex.getMessage(), ex);

        var error = ex.getBindingResult().getAllErrors().stream()  // stream() is a Java 8 api
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .sorted()
                .collect(Collectors.joining(","));  // comma separate each error message

        log.error("errorLIst : {}", error);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }


    public ResponseEntity<String> handleMovieInfoNotFoundException(MovieInfoNotFoundException ex){
        log.error("Exception caught in handleMovieInfoNotFoundException: {}", ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());

    }

}
