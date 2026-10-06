package com.airtribe.ridewise.service;

import com.airtribe.ridewise.exception.ResourceNotFoundException;
import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.Location;
import com.airtribe.ridewise.model.VehicleType;
import com.airtribe.ridewise.util.IdGenerator;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class DriverService {

    private final IdGenerator idGenerator;
    private final Map<Integer, Driver> drivers = new LinkedHashMap<>();

    public DriverService(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public Driver registerDriver(String name, Location location, VehicleType vehicleType) {
        Driver driver = new Driver(idGenerator.next("driver"), name, location, vehicleType);
        drivers.put(driver.getId(), driver);
        return driver;
    }

    public Driver getDriverById(int id) {
        Driver driver = drivers.get(id);
        if (driver == null) {
            throw new ResourceNotFoundException("Driver", id);
        }
        return driver;
    }

    public void updateAvailability(int id, boolean available) {
        getDriverById(id).setAvailable(available);
    }

    public List<Driver> getAvailableDrivers() {
        return drivers.values().stream()
                .filter(Driver::isAvailable)
                .toList();
    }

    public List<Driver> getAllDrivers() {
        return List.copyOf(drivers.values());
    }
}
