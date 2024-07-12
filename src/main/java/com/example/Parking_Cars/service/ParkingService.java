package com.example.Parking_Cars.service;
import com.example.Parking_Cars.ParkingResponse;
import com.example.Parking_Cars.enums.CarType;
import com.example.Parking_Cars.models.Car;
import com.example.Parking_Cars.models.ParkingCarsRecord;
import com.example.Parking_Cars.repos.CarRepository;
import com.example.Parking_Cars.repos.ParkingCarsRecordRepository;
import com.example.Parking_Cars.requests.NonParkingArea;
import com.example.Parking_Cars.requests.StatusRequest;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ParkingService {

    private Map<String, ParkingCarsRecord> parkingArea = new HashMap<>();
    private List<Car> nonParkingArea = new ArrayList<>();
    private int nonParkingRemainingCount = 10;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private ParkingCarsRecordRepository parkingCarsRecordRepository;

    @PostConstruct
    public void init() {
        List<ParkingCarsRecord> records = parkingCarsRecordRepository.findAll();
        for (ParkingCarsRecord record : records) {
            parkingArea.put(record.getCarType().toString(), record);
        }

        if (!parkingArea.containsKey("SMALL_CAR")) {
            ParkingCarsRecord smallPark = new ParkingCarsRecord();
            smallPark.setCarType(CarType.SMALL_CAR);
            smallPark.setFilledSlots(0);
            smallPark.setRemainingSlots(3);
            smallPark.setFilledCars(new ArrayList<>());
            parkingArea.put("SMALL_CAR", smallPark);
            parkingCarsRecordRepository.save(smallPark);
        }

        if (!parkingArea.containsKey("BIG_CAR")) {
            ParkingCarsRecord bigPark = new ParkingCarsRecord();
            bigPark.setCarType(CarType.BIG_CAR);
            bigPark.setFilledSlots(0);
            bigPark.setRemainingSlots(2);
            bigPark.setFilledCars(new ArrayList<>());
            parkingArea.put("BIG_CAR", bigPark);
            parkingCarsRecordRepository.save(bigPark);
        }
    }

    public ParkingResponse parkingEntry(Car car) {
        ParkingCarsRecord record;
        if (car.getCarType().equals(CarType.BIG_CAR)) {
            record = parkingArea.get("BIG_CAR");
        } else {
            record = parkingArea.get("SMALL_CAR");
        }

        if (record.getRemainingSlots() > 0) {
            carRepository.save(car);
            record.getFilledCars().add(car);
            record.setFilledSlots(record.getFilledSlots() + 1);
            record.setRemainingSlots(record.getRemainingSlots() - 1);
            if (car.isHandoverKeys()) {
                record.setHandOveredKeysCount(record.getHandOveredKeysCount() + 1);
            }
            parkingCarsRecordRepository.save(record);
            return new ParkingResponse(true, "Successfully added in " + record.getCarType() + " Parking Slot");
        } else if (car.getCarType().equals(CarType.SMALL_CAR) && parkingArea.get("BIG_CAR").getRemainingSlots() > 0) {
            record = parkingArea.get("BIG_CAR");
            carRepository.save(car);
            record.getFilledCars().add(car);
            record.setFilledSlots(record.getFilledSlots() + 1);
            record.setRemainingSlots(record.getRemainingSlots() - 1);
            if (car.isHandoverKeys()) {
                record.setHandOveredKeysCount(record.getHandOveredKeysCount() + 1);
            }
            parkingCarsRecordRepository.save(record);
            return new ParkingResponse(true, "Successfully added in Big Parking Slot");
        } else if (nonParkingRemainingCount > 0) {
            carRepository.save(car);
            nonParkingArea.add(car);
            nonParkingRemainingCount -= 1;
            return new ParkingResponse(true, "Successfully added in Non Parking Slot");
        }
        return new ParkingResponse(false, "No Slots Available");
    }

    public StatusRequest getParkingRequest() {
        StatusRequest status = new StatusRequest();
        status.setParkingArea(parkingArea);
        status.setNonParkingArea(new NonParkingArea(nonParkingArea, nonParkingRemainingCount));
        return status;
    }

    public String exitCars(String carId) {
        Car carToExit = carRepository.findById(carId).orElse(null);
        if (carToExit == null) {
            return "No Car Found";
        }

        String slot = null;
        ParkingCarsRecord smallPark = parkingArea.get("SMALL_CAR");
        ParkingCarsRecord bigPark = parkingArea.get("BIG_CAR");

        if (smallPark.getFilledCars().contains(carToExit)) {
            slot = "SMALL_AREA";
        } else if (bigPark.getFilledCars().contains(carToExit)) {
            slot = "BIG_AREA";
        } else if (nonParkingArea.remove(carToExit)) {
            nonParkingRemainingCount += 1;
            carRepository.delete(carToExit);
            return "Car Exited from Non-parking Area";
        } else {
            return "No Car Found";
        }

        if ("SMALL_AREA".equals(slot)) {
            smallPark.getFilledCars().remove(carToExit);
            smallPark.setFilledSlots(smallPark.getFilledSlots() - 1);
            smallPark.setRemainingSlots(smallPark.getRemainingSlots() + 1);
            if (carToExit.isHandoverKeys()) {
                smallPark.setHandOveredKeysCount(smallPark.getHandOveredKeysCount() - 1);
            }
            parkingCarsRecordRepository.save(smallPark);
        } else if ("BIG_AREA".equals(slot)) {
            bigPark.getFilledCars().remove(carToExit);
            bigPark.setFilledSlots(bigPark.getFilledSlots() - 1);
            bigPark.setRemainingSlots(bigPark.getRemainingSlots() + 1);
            if (carToExit.isHandoverKeys()) {
                bigPark.setHandOveredKeysCount(bigPark.getHandOveredKeysCount() - 1);
            }
            parkingCarsRecordRepository.save(bigPark);
        }

        shufflePlaces();

        moveNonParkingCarsToAvailableSlots();

        carRepository.delete(carToExit);
        return "Car Successfully Exited from Parking Place";
    }

    private void shufflePlaces() {
        List<Car> bigCars = parkingArea.get("BIG_CAR").getFilledCars();
        List<Car> smallCarsToMove = new ArrayList<>();

        for (Car car : bigCars) {
            if (car.getCarType().equals(CarType.SMALL_CAR)) {
                smallCarsToMove.add(car);
            }
        }

        ParkingCarsRecord smallPark = parkingArea.get("SMALL_CAR");
        ParkingCarsRecord bigPark = parkingArea.get("BIG_CAR");

        for (Car car : smallCarsToMove) {
            if (smallPark.getRemainingSlots() > 0) {
                bigPark.getFilledCars().remove(car);
                bigPark.setFilledSlots(bigPark.getFilledSlots() - 1);
                bigPark.setRemainingSlots(bigPark.getRemainingSlots() + 1);

                smallPark.getFilledCars().add(car);
                smallPark.setFilledSlots(smallPark.getFilledSlots() + 1);
                smallPark.setRemainingSlots(smallPark.getRemainingSlots() - 1);

                if (car.isHandoverKeys()) {
                    bigPark.setHandOveredKeysCount(bigPark.getHandOveredKeysCount() - 1);
                    smallPark.setHandOveredKeysCount(smallPark.getHandOveredKeysCount() + 1);
                }
            } else {
                break;
            }
        }

        parkingCarsRecordRepository.save(smallPark);
        parkingCarsRecordRepository.save(bigPark);
    }

    private void moveNonParkingCarsToAvailableSlots() {
        ParkingCarsRecord smallPark = parkingArea.get("SMALL_CAR");
        ParkingCarsRecord bigPark = parkingArea.get("BIG_CAR");
        List<Car> carsToRemove = new ArrayList<>();

        boolean bigCarWithKeysExistsInNonParking = nonParkingArea.stream()
                .anyMatch(car -> car.getCarType().equals(CarType.BIG_CAR) && car.isHandoverKeys());

        for (Car car : nonParkingArea) {
            if (car.isHandoverKeys()) {
                if (car.getCarType().equals(CarType.BIG_CAR) && bigPark.getRemainingSlots() > 0) {
                    bigPark.getFilledCars().add(car);
                    bigPark.setFilledSlots(bigPark.getFilledSlots() + 1);
                    bigPark.setRemainingSlots(bigPark.getRemainingSlots() - 1);
                    bigPark.setHandOveredKeysCount(bigPark.getHandOveredKeysCount() + 1);
                    carsToRemove.add(car);
                } else if (!bigCarWithKeysExistsInNonParking && car.getCarType().equals(CarType.SMALL_CAR) && bigPark.getRemainingSlots() > 0) {
                    bigPark.getFilledCars().add(car);
                    bigPark.setFilledSlots(bigPark.getFilledSlots() + 1);
                    bigPark.setRemainingSlots(bigPark.getRemainingSlots() - 1);
                    bigPark.setHandOveredKeysCount(bigPark.getHandOveredKeysCount() + 1);
                    carsToRemove.add(car);
                } else if (car.getCarType().equals(CarType.SMALL_CAR) && smallPark.getRemainingSlots() > 0) {
                    smallPark.getFilledCars().add(car);
                    smallPark.setFilledSlots(smallPark.getFilledSlots() + 1);
                    smallPark.setRemainingSlots(smallPark.getRemainingSlots() - 1);
                    smallPark.setHandOveredKeysCount(smallPark.getHandOveredKeysCount() + 1);
                    carsToRemove.add(car);
                }
            }
        }

        nonParkingArea.removeAll(carsToRemove);
        nonParkingRemainingCount += carsToRemove.size();
        parkingCarsRecordRepository.save(smallPark);
        parkingCarsRecordRepository.save(bigPark);
    }

}
