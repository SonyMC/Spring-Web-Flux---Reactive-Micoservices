package com.reactivespring.routes;

import com.reactivespring.handler.ReviewHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import static org.springframework.web.reactive.function.server.RouterFunctions.changeParser;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
public class ReviewRouter {

        //RouterFunction<> :Functional Interface which represents a function that routes to a handler function.
        //ServerResponse: Interface that represents a typed server-side HTTP response, as returned by a handler function or filter function

        @Bean  //needs to be a bean when spring application scans the package
        public RouterFunction<ServerResponse> reviewsRoute(ReviewHandler reviewHandler){

/*    // commented out as better option to code is by using nest function as shown below
                   return route()
                    .GET("/v1/helloworld", (request -> ServerResponse.ok().bodyValue("helloworld")))  // first parm is the routes as a string and the second parm is the handler function
                   // .POST("/v1/reviews", (request -> reviewHandler.addReview(request)))
                    .POST("/v1/reviews", (reviewHandler::addReview))
                  //  .GET("/v1/reviews", (request -> reviewHandler.getReview(request)))
                    .GET("/v1/reviews", (reviewHandler::getReview))
                    .build();
 */

            return route()
                    .nest(path("/v1/reviews"), builder -> {  // anytime we have an end point for reviews, it will be part of this nest functions
                        builder
                                 .POST("",reviewHandler::addReview)   // create
                                 .GET("",reviewHandler::getReview)    // get
                                . GET("/qp",reviewHandler::getReviewbyMovieId_QueryParm)   // get by Query Parm
                                 .PUT("/{id}",reviewHandler::updateReview) // update
                                 .PUT("/option2/{id}",reviewHandler::updateReview_Option2) // update via Option 2
                                 .DELETE("{id}",reviewHandler::deleteReview) //delete
                                 .POST("/publish",reviewHandler::addReviewPublishStream)    // Create and Publish stream
                                 .GET("/stream",reviewHandler::getReviewSubscribeStream);   //  Subscribe to Stream

                    })
                    .GET(request -> ServerResponse.ok().bodyValue("helloworld"))  // first parm is the routes as a string and the second parm is the handler function
                    .build();



        }

}
