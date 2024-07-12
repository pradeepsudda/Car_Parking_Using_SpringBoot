package com.example.Parking_Cars.controllers;

import com.example.Parking_Cars.ParkingResponse;
import com.example.Parking_Cars.models.Car;
import com.example.Parking_Cars.requests.StatusRequest;
import com.example.Parking_Cars.service.ParkingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/parkings")
@CrossOrigin(origins = "http://localhost:4200")
public class ParkingController {

    @Autowired
    ParkingService parkingService;

    @PostMapping("/entry")
    public ResponseEntity<ParkingResponse> entryParking(@RequestBody Car car) {
        ParkingResponse result = parkingService.parkingEntry(car);
        if (result.isSuccess()) {
            return new ResponseEntity<>(result, HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping
    public ResponseEntity<StatusRequest> getStatus() {
        StatusRequest status = parkingService.getParkingRequest();
        return new ResponseEntity<>(status, HttpStatus.OK);
    }

    @DeleteMapping("/exit/{carId}")
    public ResponseEntity<String> exitCars(@PathVariable String carId) {
        String result = parkingService.exitCars(carId);
        if (result.contains("Successfully Exited")) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else if (result.contains("Exited from Non-parking Area")) {
            return new ResponseEntity<>(result, HttpStatus.OK);
        } else {
            return new ResponseEntity<>(result, HttpStatus.NOT_FOUND);
        }
    }
}
