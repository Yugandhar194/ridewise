package com.airtribe.ridewise.model;

import com.airtribe.ridewise.exception.InvalidRideStateException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RideTest {

    private Driver driver;
    private Ride ride;

    @BeforeEach
    void setUp() {
        Rider rider = new Rider(1, "Asha", new Location(0, 0));
        driver = new Driver(1, "Ravi", new Location(1, 1), VehicleType.CAR);
        ride = new Ride(1, rider, 5.0);
    }

    @Test
    void newRideStartsAsRequestedWithoutDriver() {
        assertEquals(RideStatus.REQUESTED, ride.getStatus());
        assertNull(ride.getDriver());
        assertNull(ride.getFareReceipt());
    }

    @Test
    void assigningDriverMovesRideToAssignedAndMakesDriverBusy() {
        ride.assignDriver(driver);

        assertEquals(RideStatus.ASSIGNED, ride.getStatus());
        assertSame(driver, ride.getDriver());
        assertFalse(driver.isAvailable());
        assertEquals(1, driver.getTripCount());
    }

    @Test
    void rideCannotBeAssignedTwice() {
        ride.assignDriver(driver);
        Driver other = new Driver(2, "Meera", new Location(2, 2), VehicleType.BIKE);

        assertThrows(InvalidRideStateException.class, () -> ride.assignDriver(other));
        assertTrue(other.isAvailable());
    }

    @Test
    void completingStoresReceiptAndFreesDriver() {
        ride.assignDriver(driver);
        FareReceipt receipt = new FareReceipt(1, 120.0, LocalDateTime.now());

        ride.complete(receipt);

        assertEquals(RideStatus.COMPLETED, ride.getStatus());
        assertSame(receipt, ride.getFareReceipt());
        assertTrue(driver.isAvailable());
    }

    @Test
    void rideCannotBeCompletedBeforeItIsAssigned() {
        FareReceipt receipt = new FareReceipt(1, 120.0, LocalDateTime.now());

        assertThrows(InvalidRideStateException.class, () -> ride.complete(receipt));
    }

    @Test
    void cancellingAssignedRideFreesDriver() {
        ride.assignDriver(driver);

        ride.cancel();

        assertEquals(RideStatus.CANCELLED, ride.getStatus());
        assertTrue(driver.isAvailable());
    }

    @Test
    void completedRideCannotBeCancelled() {
        ride.assignDriver(driver);
        ride.complete(new FareReceipt(1, 120.0, LocalDateTime.now()));

        assertThrows(InvalidRideStateException.class, () -> ride.cancel());
    }

    @Test
    void vehicleTypeIsUnavailableUntilDriverIsAssigned() {
        assertThrows(InvalidRideStateException.class, () -> ride.getVehicleType());

        ride.assignDriver(driver);
        assertEquals(VehicleType.CAR, ride.getVehicleType());
    }
}
