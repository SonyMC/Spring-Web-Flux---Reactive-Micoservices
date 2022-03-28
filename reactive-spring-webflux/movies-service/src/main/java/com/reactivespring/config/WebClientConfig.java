package com.reactivespring.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {


    @Bean
    public WebClient webClient(WebClient.Builder builder){ // Builder is built for us by the WebClientAutoConfiguration.java
       return builder.build(); // return automatically created web client instance for pur application

    }
}
