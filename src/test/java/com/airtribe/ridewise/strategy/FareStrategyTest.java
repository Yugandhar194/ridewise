package com.airtribe.ridewise.strategy;

import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.Location;
import com.airtribe.ridewise.model.Ride;
import com.airtribe.ridewise.model.Rider;
import com.airtribe.ridewise.model.VehicleType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FareStrategyTest {

    private Ride rideWith(VehicleType type, double distance) {
        Rider rider = new Rider(1, "Asha", new Location(0, 0));
        Driver driver = new Driver(1, "Ravi", new Location(1, 1), type);
        Ride ride = new Ride(1, rider, distance);
        ride.assignDriver(driver);
        return ride;
    }

    @Test
    void defaultFareDependsOnVehicleType() {
        DefaultFareStrategy strategy = new DefaultFareStrategy();

        assertEquals(70.0, strategy.calculateFare(rideWith(VehicleType.BIKE, 5)), 0.001);   // 30 + 8 * 5
        assertEquals(90.0, strategy.calculateFare(rideWith(VehicleType.AUTO, 5)), 0.001);   // 30 + 12 * 5
        assertEquals(120.0, strategy.calculateFare(rideWith(VehicleType.CAR, 5)), 0.001);   // 30 + 18 * 5
    }

    @Test
    void defaultFareRoundsToTwoDecimals() {
        // 30 + 18 * 1.234 = 52.212
        assertEquals(52.21, new DefaultFareStrategy().calculateFare(rideWith(VehicleType.CAR, 1.234)), 0.0001);
    }

    @Test
    void peakFareMultipliesTheBaseFare() {
        FareStrategy peak = new PeakHourFareStrategy(new DefaultFareStrategy(), 1.5);

        assertEquals(180.0, peak.calculateFare(rideWith(VehicleType.CAR, 5)), 0.001);
    }

    @Test
    void peakFareWorksOnTopOfAnyFareStrategy() {
        FareStrategy flat = ride -> 100.0;   // FareStrategy has one method, so a lambda is enough
        FareStrategy peak = new PeakHourFareStrategy(flat, 2.0);

        assertEquals(200.0, peak.calculateFare(rideWith(VehicleType.BIKE, 3)), 0.001);
    }

    @Test
    void peakFareRejectsInvalidSetup() {
        assertThrows(IllegalArgumentException.class, () -> new PeakHourFareStrategy(null, 1.5));
        assertThrows(IllegalArgumentException.class, () -> new PeakHourFareStrategy(new DefaultFareStrategy(), 0.9));
        assertThrows(IllegalArgumentException.class, () -> new PeakHourFareStrategy(new DefaultFareStrategy(), Double.NaN));
    }
}
