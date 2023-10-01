package com.heydieproject.springbatchpartitioning.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CarPark {
    @Id
    @JsonProperty("car_park_no")
    private String carParkNo;
    private String address;
    @JsonProperty("x_coord")
    private Float xCoord;
    @JsonProperty("y_coord")
    private Float yCoord;
    @JsonProperty("car_park_type")
    private String carParkType;
    @JsonProperty("type_of_parking_system")
    private String typeOfParkingSystem;
    @JsonProperty("short_term_parking")
    private String shortTermParking;
    @JsonProperty("free_parking")
    private String freeParking;
    @JsonProperty("night_parking")
    private String nightParking;
    @JsonProperty("car_park_decks")
    private Integer carParkDecks;
    @JsonProperty("gantry_height")
    private Float gantryHeight;
    @JsonProperty("car_park_basement")
    private String carParkBasement;
}
