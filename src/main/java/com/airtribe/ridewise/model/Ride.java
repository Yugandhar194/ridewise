package com.airtribe.ridewise.model;

import com.airtribe.ridewise.exception.InvalidRideStateException;

/**
 * A single trip. The ride owns its own life-cycle, so the only way to move it from
 * REQUESTED -> ASSIGNED -> COMPLETED (or CANCELLED) is through the methods below.
 */
public class Ride {

    private final int id;
    private final Rider rider;
    private final double distance;
    private Driver driver;
    private RideStatus status = RideStatus.REQUESTED;
    private FareReceipt fareReceipt;

    public Ride(int id, Rider rider, double distance) {
        this.id = id;
        this.rider = rider;
        this.distance = distance;
    }

    public void assignDriver(Driver driver) {
        requireStatus(RideStatus.REQUESTED);
        driver.acceptRide();
        this.driver = driver;
        this.status = RideStatus.ASSIGNED;
    }

    public void complete(FareReceipt receipt) {
        requireStatus(RideStatus.ASSIGNED);
        driver.finishRide();
        this.fareReceipt = receipt;
        this.status = RideStatus.COMPLETED;
    }

    public void cancel() {
        if (status == RideStatus.COMPLETED || status == RideStatus.CANCELLED) {
            throw new InvalidRideStateException("Ride " + id + " is already " + status + " and cannot be cancelled");
        }
        if (driver != null) {
            driver.finishRide();
        }
        this.status = RideStatus.CANCELLED;
    }

    /** Lets fare strategies read the vehicle type without chaining ride.getDriver().getVehicleType(). */
    public VehicleType getVehicleType() {
        if (driver == null) {
            throw new InvalidRideStateException("Ride " + id + " has no driver yet");
        }
        return driver.getVehicleType();
    }

    private void requireStatus(RideStatus expected) {
        if (status != expected) {
            throw new InvalidRideStateException("Ride " + id + " is " + status + ", expected " + expected);
        }
    }

    public int getId() {
        return id;
    }

    public Rider getRider() {
        return rider;
    }

    public Driver getDriver() {
        return driver;
    }

    public double getDistance() {
        return distance;
    }

    public RideStatus getStatus() {
        return status;
    }

    public FareReceipt getFareReceipt() {
        return fareReceipt;
    }

    @Override
    public String toString() {
        String text = "Ride #" + id + " [" + status + "] rider=" + rider.getName()
                + ", driver=" + (driver == null ? "-" : driver.getName())
                + ", distance=" + distance + " km";
        if (fareReceipt != null) {
            text += ", fare=" + fareReceipt.amount();
        }
        return text;
    }
}
