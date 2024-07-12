package com.example.Parking_Cars.requests;


import com.example.Parking_Cars.models.ParkingCarsRecord;
import lombok.Data;

import java.util.Map;

@Data
public class StatusRequest {
    private Map<String, ParkingCarsRecord> parkingArea;
    private NonParkingArea nonParkingArea;
}

