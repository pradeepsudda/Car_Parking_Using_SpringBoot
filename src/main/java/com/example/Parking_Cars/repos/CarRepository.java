package com.example.Parking_Cars.repos;


import com.example.Parking_Cars.models.Car;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CarRepository extends MongoRepository<Car, String> {
}

