package com.reactivespring.service;


import com.reactivespring.domain.MovieInfo;
import com.reactivespring.repository.MovieInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class MoviesInfoService {

    @Autowired   // Auto Inject movieInfoRespository into this class
    private MovieInfoRepository movieInfoRepository;

    // Has a dependency on the MovieInfoRepository
    public Mono<MovieInfo> addMovieInfo(MovieInfo movieInfo) {
       return movieInfoRepository.save(movieInfo);   // save movieInfo into Mongo repository

    }

    public Flux<MovieInfo> getAllMovieInfos(){
        return movieInfoRepository.findAll();
    }

    public Flux<MovieInfo> getMovieInfoByYear(Integer year) {
        return movieInfoRepository.findByYear(year);
    }

    public Mono<MovieInfo> getMovieInfoByName(String name) {
        return movieInfoRepository.findByName(name);
    }

    public Mono<MovieInfo> getMovieInfoByID(String id) {
        return movieInfoRepository.findById(id);
    }

    public Mono<MovieInfo>  updateMovieInfo(MovieInfo movieInfo, String id) {

        return movieInfoRepository.findById(id)
                .flatMap(movieInfo1 -> {  // transform on element of a reactive stream to a flux
                    movieInfo1.setYear(movieInfo.getYear());
                    movieInfo1.setCast(movieInfo.getCast());
                    movieInfo1.setName(movieInfo.getName());
                    movieInfo1.setRelease_Date(movieInfo.getRelease_Date());
                    return movieInfoRepository.save(movieInfo1);   // save and return the updated Movie Info

                });
    }



    public Mono<Void> deleteMovieInfoById(String id) {
        return movieInfoRepository.deleteById(id);
     }



}
