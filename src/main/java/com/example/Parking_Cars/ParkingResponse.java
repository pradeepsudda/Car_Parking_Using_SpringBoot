package com.example.Parking_Cars;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ParkingResponse {
    private boolean success;
    private String message;
}
