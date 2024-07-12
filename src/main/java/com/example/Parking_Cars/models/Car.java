package com.example.Parking_Cars.models;

import com.example.Parking_Cars.enums.CarType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "cars")
public class Car {
    @Id
    private String carId;
    private  String carModelName;
    private CarType carType;
    @JsonProperty("isHandoverKeys")
    private boolean isHandoverKeys;
}

