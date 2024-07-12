package com.example.Parking_Cars.requests;

import com.example.Parking_Cars.models.Car;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class NonParkingArea {
    private List<Car> nonParkingArea;
    private int nonParkingRemainingCount;
}

