package com.reactivespring.domain;

// Represents the domain for Movie Info which will eb mpaeed to the DB

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;


import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;

@Data   // generates boiler plate code using lombok
@NoArgsConstructor  // Lombok
@AllArgsConstructor  // Lombok
@Document  // part of mongodb package. Is representation of an entity in a MongoDB.
public class MovieInfo {

    @Id
    private String movieInfoID;

    @NotBlank(message="movieInfo.name must be present")
    private String name;

    @NotNull
    @Positive(message = "movieInfo.year must be a positive value")
    private Integer year;

    //Since List<> is already an object we have to do something different for validation

    private List<@NotBlank(message = "movieInfo.cat must be present") String> cast;
    private LocalDate release_Date;


}
