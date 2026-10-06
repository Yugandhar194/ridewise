package com.airtribe.ridewise.model;

/** A point on a simple flat map. One unit of distance is treated as one kilometre. */
public record Location(double x, double y) {

    public Location {
        if (!Double.isFinite(x) || !Double.isFinite(y)) {
            throw new IllegalArgumentException("Location coordinates must be real numbers");
        }
    }

    public double distanceTo(Location other) {
        return Math.hypot(x - other.x, y - other.y);
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ")";
    }
}
