package com.airtribe.ridewise.model;

public class Rider {

    private final int id;
    private final String name;
    private final Location location;

    public Rider(int id, String name, Location location) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Rider name must not be empty");
        }
        if (location == null) {
            throw new IllegalArgumentException("Rider location is required");
        }
        this.id = id;
        this.name = name.trim();
        this.location = location;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }

    @Override
    public String toString() {
        return "Rider #" + id + " " + name + " at " + location;
    }
}
