package com.airtribe.ridewise.strategy;

import com.airtribe.ridewise.exception.NoDriverAvailableException;
import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.Location;
import com.airtribe.ridewise.model.Rider;
import com.airtribe.ridewise.model.VehicleType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchingStrategyTest {

    private final Rider rider = new Rider(1, "Asha", new Location(0, 0));

    private Driver driverAt(int id, double x, double y) {
        return new Driver(id, "Driver" + id, new Location(x, y), VehicleType.CAR);
    }

    // ---- NearestDriverStrategy ----

    @Test
    void nearestPicksClosestDriver() {
        Driver far = driverAt(1, 10, 10);
        Driver near = driverAt(2, 1, 1);
        Driver middle = driverAt(3, 4, 4);

        Driver chosen = new NearestDriverStrategy().findDriver(rider, List.of(far, near, middle));

        assertSame(near, chosen);
    }

    @Test
    void nearestPicksFirstDriverOnTie() {
        Driver first = driverAt(1, 3, 4);
        Driver second = driverAt(2, -3, -4);

        assertSame(first, new NearestDriverStrategy().findDriver(rider, List.of(first, second)));
    }

    @Test
    void nearestThrowsWhenNoDrivers() {
        assertThrows(NoDriverAvailableException.class,
                () -> new NearestDriverStrategy().findDriver(rider, List.of()));
    }

    // ---- LeastActiveDriverStrategy ----

    @Test
    void leastActivePicksDriverWithFewestTrips() {
        Driver busy = driverAt(1, 1, 1);
        Driver rested = driverAt(2, 50, 50);
        busy.acceptRide();
        busy.finishRide();
        busy.acceptRide();
        busy.finishRide();
        rested.acceptRide();
        rested.finishRide();

        // rested is far away but has done fewer trips, so distance must not matter here
        assertSame(rested, new LeastActiveDriverStrategy().findDriver(rider, List.of(busy, rested)));
    }

    @Test
    void leastActiveThrowsWhenNoDrivers() {
        assertThrows(NoDriverAvailableException.class,
                () -> new LeastActiveDriverStrategy().findDriver(rider, List.of()));
    }

    // ---- LSP: every implementation honours the same contract ----

    @Test
    void anyStrategyReturnsOneOfTheDriversItWasGiven() {
        List<Driver> drivers = List.of(driverAt(1, 2, 2), driverAt(2, 5, 5));
        List<RideMatchingStrategy> strategies =
                List.of(new NearestDriverStrategy(), new LeastActiveDriverStrategy());

        for (RideMatchingStrategy strategy : strategies) {
            assertTrue(drivers.contains(strategy.findDriver(rider, drivers)));
        }
    }
}
