package com.airtribe.ridewise.service;

import com.airtribe.ridewise.exception.ResourceNotFoundException;
import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.FareReceipt;
import com.airtribe.ridewise.model.Ride;
import com.airtribe.ridewise.model.Rider;
import com.airtribe.ridewise.strategy.FareStrategy;
import com.airtribe.ridewise.strategy.RideMatchingStrategy;
import com.airtribe.ridewise.util.IdGenerator;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RideService {

    private final RiderService riderService;
    private final DriverService driverService;
    private final RideMatchingStrategy matchingStrategy;
    private final FareStrategy fareStrategy;
    private final IdGenerator idGenerator;
    private final Map<Integer, Ride> rides = new LinkedHashMap<>();

    // Both strategies are interfaces: Spring (or a test) decides which implementation to pass in.
    public RideService(RiderService riderService,
                       DriverService driverService,
                       RideMatchingStrategy matchingStrategy,
                       FareStrategy fareStrategy,
                       IdGenerator idGenerator) {
        this.riderService = riderService;
        this.driverService = driverService;
        this.matchingStrategy = matchingStrategy;
        this.fareStrategy = fareStrategy;
        this.idGenerator = idGenerator;
    }

    /**
     * Creates a ride and assigns a driver straight away. If nobody is free,
     * NoDriverAvailableException is thrown and the ride is not stored.
     */
    public Ride requestRide(int riderId, double distance) {
        if (distance <= 0 || !Double.isFinite(distance)) {
            throw new IllegalArgumentException("Trip distance must be a positive number");
        }
        Rider rider = riderService.getRiderById(riderId);
        Ride ride = new Ride(idGenerator.next("ride"), rider, distance);

        Driver driver = matchingStrategy.findDriver(rider, driverService.getAvailableDrivers());
        ride.assignDriver(driver);

        rides.put(ride.getId(), ride);
        return ride;
    }

    public Ride completeRide(int rideId) {
        Ride ride = getRideById(rideId);
        double amount = fareStrategy.calculateFare(ride);
        ride.complete(new FareReceipt(ride.getId(), amount, LocalDateTime.now()));
        return ride;
    }

    public Ride cancelRide(int rideId) {
        Ride ride = getRideById(rideId);
        ride.cancel();
        return ride;
    }

    public Ride getRideById(int rideId) {
        Ride ride = rides.get(rideId);
        if (ride == null) {
            throw new ResourceNotFoundException("Ride", rideId);
        }
        return ride;
    }

    public List<Ride> getAllRides() {
        return List.copyOf(rides.values());
    }
}
