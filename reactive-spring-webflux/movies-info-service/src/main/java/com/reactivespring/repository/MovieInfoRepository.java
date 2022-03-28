package com.reactivespring.repository;

import com.reactivespring.domain.MovieInfo;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

// Interface extends ReactiveMongoRepository interface  which takes as input the domain MovieInfo and the id which is a string
public interface MovieInfoRepository extends ReactiveMongoRepository<MovieInfo,String> {

    // custom function
    Flux<MovieInfo> findByYear(Integer year);

    // custom function
    Mono<MovieInfo> findByName(String name);
}
