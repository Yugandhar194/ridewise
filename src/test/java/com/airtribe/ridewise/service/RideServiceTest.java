package com.airtribe.ridewise.service;

import com.airtribe.ridewise.exception.InvalidRideStateException;
import com.airtribe.ridewise.exception.NoDriverAvailableException;
import com.airtribe.ridewise.exception.ResourceNotFoundException;
import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.Location;
import com.airtribe.ridewise.model.Ride;
import com.airtribe.ridewise.model.RideStatus;
import com.airtribe.ridewise.model.Rider;
import com.airtribe.ridewise.model.VehicleType;
import com.airtribe.ridewise.strategy.DefaultFareStrategy;
import com.airtribe.ridewise.strategy.FareStrategy;
import com.airtribe.ridewise.strategy.LeastActiveDriverStrategy;
import com.airtribe.ridewise.strategy.NearestDriverStrategy;
import com.airtribe.ridewise.strategy.PeakHourFareStrategy;
import com.airtribe.ridewise.strategy.RideMatchingStrategy;
import com.airtribe.ridewise.util.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RideServiceTest {

    private IdGenerator ids;
    private RiderService riderService;
    private DriverService driverService;
    private Rider asha;

    @BeforeEach
    void setUp() {
        ids = new IdGenerator();
        riderService = new RiderService(ids);
        driverService = new DriverService(ids);
        asha = riderService.registerRider("Asha", new Location(0, 0));
    }

    private RideService rideServiceWith(RideMatchingStrategy matching, FareStrategy fare) {
        return new RideService(riderService, driverService, matching, fare, ids);
    }

    private RideService defaultRideService() {
        return rideServiceWith(new NearestDriverStrategy(), new DefaultFareStrategy());
    }

    @Test
    void requestRideAssignsNearestDriverAndMarksThemBusy() {
        Driver far = driverService.registerDriver("Far", new Location(9, 9), VehicleType.CAR);
        Driver near = driverService.registerDriver("Near", new Location(1, 1), VehicleType.CAR);

        Ride ride = defaultRideService().requestRide(asha.getId(), 5);

        assertEquals(RideStatus.ASSIGNED, ride.getStatus());
        assertSame(near, ride.getDriver());
        assertFalse(near.isAvailable());
        assertTrue(far.isAvailable());
    }

    @Test
    void busyDriversAreNotMatched() {
        Driver first = driverService.registerDriver("First", new Location(1, 1), VehicleType.CAR);
        Driver second = driverService.registerDriver("Second", new Location(8, 8), VehicleType.CAR);
        RideService rideService = defaultRideService();

        Ride one = rideService.requestRide(asha.getId(), 2);
        Ride two = rideService.requestRide(asha.getId(), 2);

        assertSame(first, one.getDriver());
        assertSame(second, two.getDriver());
    }

    @Test
    void requestWithoutAvailableDriverThrowsAndStoresNothing() {
        RideService rideService = defaultRideService();

        assertThrows(NoDriverAvailableException.class, () -> rideService.requestRide(asha.getId(), 5));
        assertEquals(0, rideService.getAllRides().size());
    }

    @Test
    void requestRejectsUnknownRiderAndBadDistance() {
        driverService.registerDriver("Ravi", new Location(1, 1), VehicleType.CAR);
        RideService rideService = defaultRideService();

        assertThrows(ResourceNotFoundException.class, () -> rideService.requestRide(42, 5));
        assertThrows(IllegalArgumentException.class, () -> rideService.requestRide(asha.getId(), 0));
        assertThrows(IllegalArgumentException.class, () -> rideService.requestRide(asha.getId(), -3));
        assertThrows(IllegalArgumentException.class, () -> rideService.requestRide(asha.getId(), Double.NaN));
    }

    @Test
    void completeRideChargesFareAndFreesDriver() {
        Driver ravi = driverService.registerDriver("Ravi", new Location(1, 1), VehicleType.CAR);
        RideService rideService = defaultRideService();
        Ride ride = rideService.requestRide(asha.getId(), 5);

        Ride completed = rideService.completeRide(ride.getId());

        assertEquals(RideStatus.COMPLETED, completed.getStatus());
        assertEquals(120.0, completed.getFareReceipt().amount(), 0.001);
        assertEquals(ride.getId(), completed.getFareReceipt().rideId());
        assertTrue(ravi.isAvailable());
    }

    @Test
    void rideCannotBeCompletedTwice() {
        driverService.registerDriver("Ravi", new Location(1, 1), VehicleType.CAR);
        RideService rideService = defaultRideService();
        Ride ride = rideService.requestRide(asha.getId(), 5);
        rideService.completeRide(ride.getId());

        assertThrows(InvalidRideStateException.class, () -> rideService.completeRide(ride.getId()));
    }

    @Test
    void completeUnknownRideThrows() {
        assertThrows(ResourceNotFoundException.class, () -> defaultRideService().completeRide(5));
    }

    @Test
    void cancelRideFreesDriverAndBlocksCompletion() {
        Driver ravi = driverService.registerDriver("Ravi", new Location(1, 1), VehicleType.CAR);
        RideService rideService = defaultRideService();
        Ride ride = rideService.requestRide(asha.getId(), 5);

        rideService.cancelRide(ride.getId());

        assertEquals(RideStatus.CANCELLED, ride.getStatus());
        assertTrue(ravi.isAvailable());
        assertThrows(InvalidRideStateException.class, () -> rideService.completeRide(ride.getId()));
    }

    @Test
    void allRidesAreListedInBookingOrder() {
        driverService.registerDriver("A", new Location(1, 1), VehicleType.CAR);
        driverService.registerDriver("B", new Location(2, 2), VehicleType.CAR);
        RideService rideService = defaultRideService();

        Ride one = rideService.requestRide(asha.getId(), 1);
        Ride two = rideService.requestRide(asha.getId(), 1);

        assertEquals(List.of(one, two), rideService.getAllRides());
    }

    // ---- OCP: same RideService code, different behaviour, just by passing different strategies ----

    @Test
    void swappingMatchingStrategyChangesWhoGetsTheRide() {
        Driver near = driverService.registerDriver("Near", new Location(1, 1), VehicleType.CAR);
        Driver far = driverService.registerDriver("Far", new Location(9, 9), VehicleType.CAR);
        // give the near driver one finished trip so they count as more active
        near.acceptRide();
        near.finishRide();

        Ride byDistance = rideServiceWith(new NearestDriverStrategy(), new DefaultFareStrategy())
                .requestRide(asha.getId(), 2);
        assertSame(near, byDistance.getDriver());
        byDistance.cancel();

        Ride byActivity = rideServiceWith(new LeastActiveDriverStrategy(), new DefaultFareStrategy())
                .requestRide(asha.getId(), 2);
        assertSame(far, byActivity.getDriver());
    }

    @Test
    void swappingFareStrategyChangesTheFare() {
        driverService.registerDriver("Ravi", new Location(1, 1), VehicleType.CAR);
        RideService peakService = rideServiceWith(new NearestDriverStrategy(),
                new PeakHourFareStrategy(new DefaultFareStrategy(), 1.5));
        Ride ride = peakService.requestRide(asha.getId(), 5);

        assertEquals(180.0, peakService.completeRide(ride.getId()).getFareReceipt().amount(), 0.001);
    }
}
