package com.reactivespring.controller.sinks;

import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

public class SinksTest {

   // Refer https://projectreactor.io/docs/core/release/reference/#processors

    @Test
    void sink(){

        //given
        //Publish & Subscribe: Sink publishes multiple events and will replay all events once a subscriber is connected to it
        Sinks.Many<Integer> replaySink = Sinks.many().replay().all();  //// replay().all() : anytime a new subscriber is added it will replay al levents

        //when
        // Manual triggering:
        replaySink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST); //emit a signal manually and in case of failure fail fast
        replaySink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        //then
        // Access and Subscribe to the published events using replaySink
        //Subscription:

        // 1st Subscriber
        //Access:
        Flux<Integer> integerFlux1 = replaySink.asFlux(); //  access the published events
        //Subscribe:
        integerFlux1.subscribe((i) -> {
            System.out.println("Subscriber 1 : " + i);  // print the published events
        });

        // 2nd Subscriber
        //Access:
        Flux<Integer> integerFlux2 = replaySink.asFlux(); //  access the published events
        //Subscribe:
        integerFlux2.subscribe((i) -> {
            System.out.println("Subscriber 2 : " + i);  // print the two events published events
        });

        // Manual triggering using tryEmitNext()
        // Note: The above two subscribers will automatically receive this event. i.e. 3 will be output on al subscribers!!!
        replaySink.tryEmitNext(3); // tryEventNext() automatically takes care of the Failure Handler and so no explicit FailureHandler is required


        // 3rd Subscriber
        //Access:
        Flux<Integer> integerFlux3 = replaySink.asFlux(); //  access the published events
        //Subscribe:
        integerFlux3.subscribe((i) -> {
            System.out.println("Subscriber 3 : " + i);  // print the two events published events
        });


    }

    @Test
    void sinks_multicast(){

        //given
        //Publish & Subscribe: Sink publishes multiple events
        // multicast: a sink that will transmit only newly pushed data to its subscribers, honoring their backpressure (newly pushed as in "after the subscriber’s subscription").
        Sinks.Many<Integer> muliticastSink = Sinks.many().multicast().onBackpressureBuffer();


        // Manual triggering:
        muliticastSink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST); //emit a signal manually and in case of failure fail fast
        muliticastSink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        //then
        // Access and Subscribe to the published events using replaySink
        //Subscription:

        // 1st Subscriber :Will get all events( here 1,2,3)
        //Access:
        Flux<Integer> integerFlux1 = muliticastSink.asFlux(); //  access the published events
        //Subscribe:
        integerFlux1.subscribe((i) -> {
            System.out.println("Subscriber 1 : " + i);  // print the published events
        });

        // 2nd Subscriber : Will get only new events( here 2)
        //Access:
        Flux<Integer> integerFlux2 = muliticastSink.asFlux(); //  access the published events
        //Subscribe:
        integerFlux2.subscribe((i) -> {
            System.out.println("Subscriber 2 : " + i);  // print the two events published events
        });

        // Manual triggering using tryEmitNext()
        // Note: The above two subscribers will automatically receive this event. i.e. 3 will be output on al subscribers!!!
        muliticastSink.tryEmitNext(3); // tryEventNext() automatically takes care of the Failure Handler and so no explicit FailureHandler is required


    }


    @Test
    void sinks_unicast(){

        //given
        //Publish & Subscribe: Sink publishes multiple events
        //Unicast: same as multicast  above, with the twist that data pushed before the first subscriber registers is buffered
        // Note: Unicast will allow only one subscriber
        Sinks.Many<Integer> unicastSink = Sinks.many().unicast().onBackpressureBuffer();


        // Manual triggering:
        unicastSink.emitNext(1, Sinks.EmitFailureHandler.FAIL_FAST); //emit a signal manually and in case of failure fail fast
        unicastSink.emitNext(2, Sinks.EmitFailureHandler.FAIL_FAST);

        //then
        // Access and Subscribe to the published events using replaySink
        //Subscription:

        // 1st Subscriber :Will get all events( here 1,2,3)
        //Access:
        Flux<Integer> integerFlux1 = unicastSink.asFlux(); //  access the published events
        //Subscribe:
        integerFlux1.subscribe((i) -> {
            System.out.println("Subscriber 1 : " + i);  // print the published events
        });


        // Manual triggering using tryEmitNext()
        // Note: The above two subscribers will automatically receive this event. i.e. 3 will be output on 1st subscriber.
        unicastSink.tryEmitNext(3); // tryEventNext() automatically takes care of the Failure Handler and so no explicit FailureHandler is required


    }

}
