package com.airtribe.ridewise.strategy;

import com.airtribe.ridewise.model.Ride;

/** Base fare plus a per-kilometre rate that depends on the vehicle. */
public class DefaultFareStrategy implements FareStrategy {

    private static final double BASE_FARE = 30.0;

    @Override
    public double calculateFare(Ride ride) {
        double ratePerKm = switch (ride.getVehicleType()) {
            case BIKE -> 8.0;
            case AUTO -> 12.0;
            case CAR -> 18.0;
        };
        double fare = BASE_FARE + ratePerKm * ride.getDistance();
        return Math.round(fare * 100) / 100.0;
    }
}
