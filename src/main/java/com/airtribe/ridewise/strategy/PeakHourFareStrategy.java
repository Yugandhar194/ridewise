package com.airtribe.ridewise.strategy;

import com.airtribe.ridewise.model.Ride;

/**
 * Surge pricing. It wraps another FareStrategy and multiplies its result, so it works
 * with any base pricing instead of inheriting from DefaultFareStrategy.
 */
public class PeakHourFareStrategy implements FareStrategy {

    private final FareStrategy baseStrategy;
    private final double multiplier;

    public PeakHourFareStrategy(FareStrategy baseStrategy, double multiplier) {
        if (baseStrategy == null) {
            throw new IllegalArgumentException("A base fare strategy is required");
        }
        if (!(multiplier >= 1.0)) {
            throw new IllegalArgumentException("Peak multiplier must be at least 1.0");
        }
        this.baseStrategy = baseStrategy;
        this.multiplier = multiplier;
    }

    @Override
    public double calculateFare(Ride ride) {
        double fare = baseStrategy.calculateFare(ride) * multiplier;
        return Math.round(fare * 100) / 100.0;
    }
}
