package com.reactivespring.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.test.StepVerifier;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;


@WebFluxTest(controllers = FluxAndMonoController.class )    // taken in parm controller
@AutoConfigureWebTestClient  //makes sure that the web test client instance is automatically injected into this class
class FluxAndMonoControllerTest {

    @Autowired  // autowire the web test client instance
    WebTestClient webTestClient ;


    @Test
    void flux_approach1() {

        webTestClient
                .get()
                .uri("/flux")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Integer.class)
                .hasSize(3);  // assert the response has a list of 3 integers

    }

    @Test
    void flux_approach2() {

        var flux = webTestClient
                .get()
                .uri("/flux")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(Integer.class)
                .getResponseBody();  // get the entire response body

        // Now use step verifier to individually confirm values
        StepVerifier.create(flux)
                .expectNext(1,2,3)
                .verifyComplete();

    }

    @Test
    void flux_approach3() {

        webTestClient
                .get()
                .uri("/flux")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBodyList(Integer.class)
                .consumeWith(ListEntityExchangeResult ->{
                    var responseBody = ListEntityExchangeResult.getResponseBody();
                    assert (Objects.requireNonNull(responseBody).size() == 3);  // check body is not null and size is 3.

                } );


    }

    @Test
    void mono_approach1() {

        webTestClient
                .get()
                .uri("/mono")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(String.class)
                .isEqualTo("Keep Roaring!!!");
    }



    @Test
    void mono_approach2() {

        var flux = webTestClient
                .get()
                .uri("/mono")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(String.class)
                .getResponseBody();

// Now use step verifier to individually confirm values
        StepVerifier.create(flux)
                .expectNext("Keep Roaring!!!")
                .verifyComplete();

    }

    @Test
    void mono_approach3() {

        webTestClient
                .get()
                .uri("/mono")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .expectBody(String.class)
                .consumeWith(StringEntityExchangeResult ->{
                    var responseBody = StringEntityExchangeResult.getResponseBody();
                    assertEquals("Keep Roaring!!!", responseBody);  // assert variation 1
                    assert (Objects.requireNonNull(responseBody).equals("Keep Roaring!!!")); // assert variation 2 with non null check

                } );


    }


    // Note: This is a tricky one as it will keep generating output. To avoid this we have to explicitly cancel!!!
    @Test
    void stream() {

        var flux = webTestClient
                .get()
                .uri("/stream")
                .exchange()
                .expectStatus()
                .is2xxSuccessful()
                .returnResult(Long.class)
                .getResponseBody();  // get the entire response body

        // Now use step verifier to individually confirm values
        StepVerifier.create(flux)
                .expectNext(0L,1L,2L,3L)
                .thenCancel()   // Cancel teh operation as else it will keep geernating output
                .verify();  // Note : use verify() instead of verifyComplete() as we have cannot complete

    }
}