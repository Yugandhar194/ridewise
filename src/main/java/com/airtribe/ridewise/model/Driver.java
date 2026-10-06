package com.airtribe.ridewise.model;

public class Driver {

    private final int id;
    private final String name;
    private final VehicleType vehicleType;
    private final Location currentLocation;
    private boolean available = true;
    private int tripCount;

    public Driver(int id, String name, Location currentLocation, VehicleType vehicleType) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Driver name must not be empty");
        }
        if (currentLocation == null) {
            throw new IllegalArgumentException("Driver location is required");
        }
        if (vehicleType == null) {
            throw new IllegalArgumentException("Vehicle type is required");
        }
        this.id = id;
        this.name = name.trim();
        this.currentLocation = currentLocation;
        this.vehicleType = vehicleType;
    }

    /** Lets strategies ask the driver for a distance instead of reaching into currentLocation themselves. */
    public double distanceFrom(Location point) {
        return currentLocation.distanceTo(point);
    }

    /** Called when a ride is assigned: the driver becomes busy and the trip counts towards their activity. */
    public void acceptRide() {
        available = false;
        tripCount++;
    }

    /** Called when the ride ends (completed or cancelled): the driver can take new rides again. */
    public void finishRide() {
        available = true;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public int getTripCount() {
        return tripCount;
    }

    @Override
    public String toString() {
        return "Driver #" + id + " " + name + " (" + vehicleType + ") at " + currentLocation
                + ", " + (available ? "available" : "busy") + ", trips=" + tripCount;
    }
}
