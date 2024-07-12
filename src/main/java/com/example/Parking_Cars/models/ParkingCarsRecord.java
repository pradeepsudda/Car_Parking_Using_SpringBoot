package com.example.Parking_Cars.models;

import com.example.Parking_Cars.enums.CarType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "parking_records")
public class ParkingCarsRecord {
    @Id
    private String id;
    private CarType carType;
    private int filledSlots;
    private int remainingSlots;
    private int handOveredKeysCount;
    private List<Car> filledCars;
}

