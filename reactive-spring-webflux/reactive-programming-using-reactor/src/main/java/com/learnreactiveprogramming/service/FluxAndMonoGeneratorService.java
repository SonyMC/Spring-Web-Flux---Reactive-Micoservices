package com.learnreactiveprogramming.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuple5;

import java.time.Duration;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;
import java.util.function.Function;

public class FluxAndMonoGeneratorService {

    int stringLength =3;

 /*
 *       GENERIC functions
 *
 * */

    // Function to split the provided string into individual chars and return as a FLux
    public Flux<String> splitString(String name) {
        //split the provided string into an array
       var charArray= name.split(""); // split based on the supplied delimiter . Here we are interested in extracting all individual chars in the string
       return Flux.fromArray(charArray); // convert and return as Flux
    }

    // Introduce a time delay to split the provided string into individual chars and return as a FLux
    public Flux<String> splitString_withDelay(String name) {

        // get a random integer between 0 to 1000 which will eb used by the .delayElements call
        var delay = new Random().nextInt(1000);

        //var delay =1000;  // 1 second

        //split the provided string into an array
        var charArray= name.split(""); // split based on the supplied delimiter . Here we are interested in extracting all individual chars in the string
        return Flux.fromArray(charArray) // convert and return as Flux
                .delayElements(Duration.ofMillis(delay));        // duration by which to delay each Subscriber.onNext signal
    }


    // Function to split the provided string into individual chars and return as a Mono
    private Mono<List<String>> splitStringMono(String s) {
        var charArray = s.split("");  // split string and store in char array
        var charList = List.of(charArray); // Create List from char array
        return Mono.just(charList);
    }




    /*
     *    FLUX
     *
     * */

    //METHOD - to demo Flux
    // Datasource 1 - Publisher
    //returns a Flux(reactive type that represents 0 to N elements ) of String
    public Flux<String> namesFlux(){
        //fromIterable() takes a collection
        //create and return a flux using the collection
        //return Flux.fromIterable(List.of("alex","ben","chloe"));  // though hard-coded here, the collection will usually be returned from a DB or service call
        return Flux.fromIterable(List.of("alex","ben","chloe"))
                .log();  // log each event between teh publisher and subscriber
    }

    // METHOD : to demo map operator on Flux reactive type
    public Flux<String> namesFlux_map() {
        // convert each flux element from lower to upper
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                // .map method accepts as a parameter the functional interface 'Function.java' which has an abstract apply method.
                // **** longer version of lambda function
//                .map(s -> {
//                    return s.toUpperCase();
//                })
                // *** shorter version of lambda function
//              .map(s -> s.toUpperCase() )
//              .map((s -> s.toUpperCase() ))
                // **** method reference to String class's toUpperCase function
                .map(String::toUpperCase)
                .log();
    }


    // METHOD : to demo immutability on Flux reactive type
    // Note : Here since we are trying to assign the Reactive stream returned from the Flux to a variable and then apply the mapper, it will not transform the strings to upper cases due to immutability.
    public Flux<String> namesFlux_immutability() {
        // convert each flux element from lower to upper
        var namesFlux = Flux.fromIterable(List.of("alex", "ben", "chloe"));
        // .map method accepts as a parameter the functional interface 'Function.java' which has an abstract apply method.
        // **** method reference to String class's toUpperCase function
        namesFlux.map(String::toUpperCase)
                .log();
        return namesFlux;
    }


    // METHOD : to demo filter operator on Flux reactive type
    public Flux<String> namesFlux_mapAndFilter() {


        // convert each flux element from lower to upper
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                // .map method accepts as a parameter the functional interface 'Function.java' which has an abstract apply method.
                // **** method reference to String class's toUpperCase function
                .map(String::toUpperCase)
                // *** filter the string whose length is greater than 3 ( i.e. allow only strings of length greater than 3)
                //.filter(s -> s.length() > 3)
                .filter(s -> s.length() > stringLength)
                .map(s -> s.length() + "-" + s) //prefix string with length
                .log();
    }


    // METHOD : to demo flatMap operator on Flux reactive type
    public Flux<String> flux_flatMap() {


        // convert each flux element from lower to upper
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                // .map method accepts as a parameter the functional interface 'Function.java' which has an abstract apply method.
                // **** method reference to String class's toUpperCase function
                .map(String::toUpperCase)
                // *** filter the string whose length is greater than 3 ( i.e. allow only strings of length greater than 3)
                //.filter(s -> s.length() > 3)
                .filter(s -> s.length() > stringLength)
                //return individual characters of the names viz. A,L,E,X,C,H,L,O,E
                .flatMap( s -> splitString(s))  // the splitString function returns a Flux of individual components
                    .log();
    }


    // METHOD : to demo asynchronous operations on flatMap operator on Flux reactive type
    public Flux<String> flux_flatMap_async() {


        // convert each flux element from lower to upper
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                // .map method accepts as a parameter the functional interface 'Function.java' which has an abstract apply method.
                // **** method reference to String class's toUpperCase function
                .map(String::toUpperCase)
                // *** filter the string whose length is greater than 3 ( i.e. allow only strings of length greater than 3)
                //.filter(s -> s.length() > 3)
                .filter(s -> s.length() > stringLength)
                //return individual characters of the names viz. A,L,E,X,C,H,L,O,E
                .flatMap( s -> splitString_withDelay(s))  // the splitString function returns a Flux of individual components with a delay
                .log();
    }


    // METHOD : to demo synchronous operations using concatMAp() operator on Flux reactive type
    public Flux<String> flux_concatMap() {


        // convert each flux element from lower to upper
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                // .map method accepts as a parameter the functional interface 'Function.java' which has an abstract apply method.
                // **** method reference to String class's toUpperCase function
                .map(String::toUpperCase)
                // *** filter the string whose length is greater than 3 ( i.e. allow only strings of length greater than 3)
                //.filter(s -> s.length() > 3)
                .filter(s -> s.length() > stringLength)
                //return individual characters of the names viz. A,L,E,X,C,H,L,O,E
                .concatMap( s -> splitString_withDelay(s))  // the splitString function returns a Flux of individual components with a delay
                .log();
    }





    // METHOD : to demo transform operator on Flux reactive type
    // Accepts a Function Functional Interface
    public Flux<String> flux_transform() {


        // Function Functional Interfaces are used to extract a functionality and assign that functionality to a variable
        // Transform function accepts a Function Functional interface where both Input and Output are Flux
        Function<Flux<String>,Flux<String>> filtermap = name -> name.map(String::toUpperCase)  //**** method reference to String class's toUpperCase function
                .filter(s -> s.length() > stringLength)  // *** filter the string whose length is greater than 3 ( i.e. allow only strings of length greater than 3)
                .flatMap( s -> splitString(s))  //return individual characters of the names viz. A,L,E,X,C,H,L,O,E
                .defaultIfEmpty("default"); // specify a default value if no value is returned

        // convert each flux element from lower to upper
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .transform(filtermap)
                .log();
    }

    // METHOD : to demo transform operator with Default Switch on Flux reactive type
    // Accepts a Function Functional Interface
    public Flux<String> flux_transform_switchDefault() {

        int stringLength =6;

        // Function Functional Interfaces are used to extract a functionality and assign that functionality to a variable
        // Transform function accepts a Function Functional interface where both Input and Output are Flux
        Function<Flux<String>,Flux<String>> filtermap = name -> name.map(String::toUpperCase)  //**** method reference to String class's toUpperCase function
                .filter(s -> s.length() > stringLength)  // *** filter the string whose length is greater than 6 ( i.e. allow only strings of length greater than 6)
                .flatMap( s -> splitString(s))
                .defaultIfEmpty("default"); // specify a default value if no value is returned

        // convert each flux element from lower to upper
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .transform(filtermap)   // will return default value as no names have length greater than 6
                .log();
    }


    // METHOD : to demo transform operator with Empty Switch on Flux reactive type
    // Accepts a Function Functional Interface
    public Flux<String> flux_transform_switchifEmpty() {

        int stringLength =6;

        // Function Functional Interfaces are used to extract a functionality and assign that functionality to a variable
        // Transform function accepts a Function Functional interface where both Input and Output are Flux
        Function<Flux<String>,Flux<String>> filtermap = name -> name.map(String::toUpperCase)  //**** method reference to String class's toUpperCase function
                .filter(s -> s.length() > stringLength)  // *** filter the string whose length is greater than 3 ( i.e. allow only strings of length greater than 3)
                .flatMap( s -> splitString(s));  //return individual characters of the names viz. A,L,E,X,C,H,L,O,E


        // Create a Publisher Flux which can be used as input to the switchIf operator
       var defaultFlux = Flux.just("default")
               .transform(filtermap);   // reuse Function Functional Interface filtermap

        // convert each flux element from lower to upper
        return Flux.fromIterable(List.of("alex", "ben", "chloe"))
                .transform(filtermap)  //// will return default value as no names have length greater than 6
                .switchIfEmpty(defaultFlux)   // accepts only a Publisher
                .log();
    }



    // Create a Publisher Flux to explore concat()
    public Flux<String> explore_concat(){
        var abcFlux = Flux.just("A","B","C");  // create a flux for A,B,C
        var defFlux = Flux.just("D","E","F");  // create a flux fro D,E,F

        return Flux.concat(abcFlux,defFlux) // concat is a Static Method
                .log();
    }


    // Create a Publisher Flux to explore concatWith()
    public Flux<String> explore_concatWith(){
        var abcFlux = Flux.just("A","B","C");  // create a flux for A,B,C
        var defFlux = Flux.just("D","E","F");  // create a flux fro D,E,F

        return abcFlux.concatWith(defFlux)  // concatWith is an Instance Method
                .log();
    }

    // Create a Publisher Flux to explore merge()
    public Flux<String> explore_merge(){
        var abcFlux = Flux.just("A","B","C")  // create a flux for A,B,C
                .delayElements(Duration.ofMillis(100)); // the parallel processing wnt be evident unless we introduce a selay
        var defFlux = Flux.just("D","E","F")  // create a flux fro D,E,F
                .delayElements(Duration.ofMillis(125));
        return Flux.merge(abcFlux,defFlux) // merge is a Static Method
                .log();
    }


    // Create a Publisher Flux to explore mergeWith()
    public Flux<String> explore_mergetWith(){
        var abcFlux = Flux.just("A","B","C")  // create a flux for A,B,C
                .delayElements(Duration.ofMillis(100));
        var defFlux = Flux.just("D","E","F")  // create a flux fro D,E,F
                .delayElements(Duration.ofMillis(125));
        return abcFlux.mergeWith(defFlux)  // mergeWith is an Instance Method
                .log();
    }

    // Create a Publisher Flux to explore mergeSequential()
    public Flux<String> explore_mergeSequential(){
        var abcFlux = Flux.just("A","B","C")  // create a flux for A,B,C
                .delayElements(Duration.ofMillis(100));
        var defFlux = Flux.just("D","E","F")  // create a flux fro D,E,F
                .delayElements(Duration.ofMillis(125));
        return Flux.mergeSequential(abcFlux,defFlux) // mergeSequential is a Static Method
                .log();
    }

    // Create a Publisher Flux to explore zip()
    public Flux<String> explore_zip(){
        var abcFlux = Flux.just("A","B","C");  // create a flux for A,B,C

        var defFlux = Flux.just("D","E","F");  // create a flux for D,E,F

        return Flux.zip(abcFlux,defFlux,(first,second) -> first + second)     // zip is a Static Method, Result is going to be AD,BE,CF
                .log();
    }


    // Create a Publisher Flux to explore zip() for a tuple
    // handles a max. of 8 elements
    public Flux<String> explore_zip_tuple(){
        var abcFlux = Flux.just("A","B","C");  // create a flux for A,B,C

        var defFlux = Flux.just("D","E","F");  // create a flux for D,E,F

        var _123Flux = Flux.just("1","2","3");  // create a flux for 1,2,3

        var _456Flux = Flux.just("4","5","6");  // create a flux for 4,5,6


        return Flux.zip(abcFlux,defFlux,_123Flux,_456Flux)     // zip is a Static Method
                .map(Tuple4 -> Tuple4.getT1() + Tuple4.getT2() + Tuple4.getT3() + Tuple4.getT4())  // output will be AD14, BE25, CF36
                .log();
    }


    // Create a Publisher Flux to explore zipWith()
    public Flux<String> explore_zipWith(){
        var abcFlux = Flux.just("A","B","C");  // create a flux for A,B,C

        var defFlux = Flux.just("D","E","F");  // create a flux for D,E,F

        return abcFlux.zipWith(defFlux,(first,second) -> first + second)     // zipWith is an Instance Method, Result is going to be AD,BE,CF)
                .log();

    }



    /*
*    MONO
*
* */
    //METHOD - to demo Mono
    // Datasource 2 - Publisher
    //returns a mono (reactive type that represents 1 element) of String
    public Mono<String> namesMono(){
        // 'just' emits a mono with the specified item
        return Mono.just("achudhan")
                .log(); // log each event between teh publisher and subscriber
    }

    // METHOD : to demo map operator on mono reactive type
    public Mono<String> namesMono_map() {
        // convert Mono stream from lower to upper
        return Mono.just("achudhan")
                // .map method accepts as a parameter the functional interface 'Function.java' which has an abstract apply method.
                .map(String::toUpperCase)
                .log();
    }


    // METHOD : to demo immutability on Mono reactive type
    // Note : Here since we are trying to assign the Reactive stream returned from the Mono to a variable and then apply the mapper, it will not transform the strings to upper cases due to immutability.
    public Mono<String> namesMono_immutability() {
        // try to convert Mono stream from lower to upper
        var namesMono = Mono.just("achudhan");
        // .map method accepts as a parameter the functional interface 'Function.java' which has an abstract apply method.
        // **** method reference to String class's toUpperCase function
        namesMono.map(String::toUpperCase)
                .log();
        return namesMono;
    }


    // METHOD : to demo filter operator on Mono reactive type
    public Mono<String> namesMono_mapAndFilter() {
        // convert each flux element from lower to upper

        return Mono.just("achudhan")
                // .map method accepts as a parameter the functional interface 'Function.java' which has an abstract apply method.
                // **** method reference to String class's toUpperCase function
                .map(String::toUpperCase)
                // *** filter the string whose length is greater than 3 ( i.e. allow only strings of length greater than 3)
                // .filter(s -> s.length() > 3)
                .filter(s-> s.length() > stringLength)  //filter ;
                .map(s -> s.length() + "-" + s) //prefix string with length
                .log();
    }

    // METHOD : to demo flatMap operator on Mono reactive type
    public Mono<List<String>> namesMono_flatMap() {
        // convert each flux element from lower to upper

        return Mono.just("achudhan")
                // .map method accepts as a parameter the functional interface 'Function.java' which has an abstract apply method.
                // **** method reference to String class's toUpperCase function
                .map(String::toUpperCase)
                // *** filter the string whose length is greater than 3 ( i.e. allow only strings of length greater than 3)
                .filter(s-> s.length() > stringLength)  //filter ;
                .map(s -> s.length() + "-" + s) //prefix string with length
                .flatMap(this::splitStringMono) // flatMap() accepts as a parameter a functional interface. Method reference to function within this class which will return  Mono list of 8,-,A,C.H,U,D,H,A,N
                .log();
    }

    // METHOD : to demo flatMapMany operator on Mono reactive type which will return a Flux
    public Flux<String> namesMono_flatMapMany() {
        // convert each flux element from lower to upper

        return Mono.just("achudhan")
                // .map method accepts as a parameter the functional interface 'Function.java' which has an abstract apply method.
                // **** method reference to String class's toUpperCase function
                .map(String::toUpperCase)
                // *** filter the string whose length is greater than 3 ( i.e. allow only strings of length greater than 3)
                .filter(s-> s.length() > stringLength)  //filter ;
                .map(s -> s.length() + "-" + s) //prefix string with length
                .flatMapMany(this::splitString) // flatMap() accepts as a parameter a functional interface. Method reference to function within this class which will return  Flux ist of 8,-,A,C.H,U,D,H,A,N
                .log();
    }


    // Create a Publisher Mono to explore concatWith()
    public Flux<String> explore_concatWithMono(){
        var aMono = Mono.just("A");  // create a mono for A
        var bMono = Mono.just("B");  // create a mono for B

        return aMono.concatWith(bMono)  // concatWith is an Instance emthod. Returns a Flux.
                .log();
    }

    // Create a Publisher Mono to explore mergeWith()
    public Flux<String> explore_mergeWithMono(){
        var aMono = Mono.just("A");         // create a mono for A
        var bMono = Mono.just("B") ;

        return aMono.mergeWith(bMono)  // concatWith is an Instance method. Returns a Flux.
                .log();
    }


    // Create a Publisher Mono to explore zip()
    public Flux<String> explore_zipMono(){
        var aMono = Mono.just("A");  // create a mono for A

        var bMono = Mono.just("B");  // create a mono for B


        return Flux.zip(aMono,bMono,(first,second) -> first + second )     // zip is a Static Method, Result is going to be AB
                .log();
    }



    // Create a Publisher Mono to explore zip() for a tuple
    // handles a max. of 8 elements
    public Flux<String> explore_zip_tuple_mono(){
        var aMono = Mono.just("A");  // create a mono for A

        var bMono = Mono.just("B");  // create a mono for B

        var cMono = Mono.just("C");  // create a mono for C


        return Flux.zip(aMono,bMono,cMono)     // zip is a Static Method
                .map(Tuple3 -> Tuple3.getT1() + Tuple3.getT2() + Tuple3.getT3())  // output willl be ABC
                .log();
    }


    // Create a Publisher Mono to explore zipWith()
    public Mono<String> explore_zipWithMono(){
        var aMono = Mono.just("A");  // create a mono for A

        var bMono =Mono.just("B");  // create a mono for B

        return aMono.zipWith(bMono)     // zipWith is an Instance Method. Result is going to be AB
                .map(Tuple2 -> Tuple2.getT1() + Tuple2.getT2())
                .log();

    }

    // MAIN METHOD
    public static void main(String[] args) {

        //Create an instance of the present class
        FluxAndMonoGeneratorService fluxAndMonoGeneratorService = new FluxAndMonoGeneratorService();

        // Subscriber
        // Access the Flux
        //In order to access the flux created in the method above we have to subscribe to it
        //Consume the values returned by the flux and print it out
        //The subscribe method uses as a parameter a functional interface named Consumer .
        //The function interface Consumer has one method 'accept' the implementation of which we will define using a lambda
        // The parameter of the accept method is generic and we will use the console print as the method implementation
         fluxAndMonoGeneratorService.namesFlux()
                .subscribe(name -> {
                    System.out.println("Flux names are:" + name);
        });

        // Access the Mono
        //In order to access the mono created in the method above we have to subscribe to it
        //Consume the value returned by the  mono and print it out
        //The subscribe method uses as a parameter a functional interface named Consumer .
        //The function interface Consumer has one method 'accept' the implementation of which we will define using a lambda
        // The parameter of the accept method is generic and we will use the console print as the method implementation
        fluxAndMonoGeneratorService.namesMono()
                .subscribe(name -> {
                    System.out.println(("Mono Name is :" + name));
                });



    }
}
