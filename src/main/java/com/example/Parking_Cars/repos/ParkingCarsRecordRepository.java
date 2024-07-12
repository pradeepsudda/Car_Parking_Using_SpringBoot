package com.example.Parking_Cars.repos;

import com.example.Parking_Cars.models.ParkingCarsRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ParkingCarsRecordRepository extends MongoRepository<ParkingCarsRecord, String> {
    ParkingCarsRecord findByCarType(String carType);
}

