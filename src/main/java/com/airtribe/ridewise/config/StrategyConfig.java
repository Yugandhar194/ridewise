package com.airtribe.ridewise.config;

import com.airtribe.ridewise.strategy.DefaultFareStrategy;
import com.airtribe.ridewise.strategy.FareStrategy;
import com.airtribe.ridewise.strategy.LeastActiveDriverStrategy;
import com.airtribe.ridewise.strategy.NearestDriverStrategy;
import com.airtribe.ridewise.strategy.PeakHourFareStrategy;
import com.airtribe.ridewise.strategy.RideMatchingStrategy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * The one place that knows which concrete strategies exist. Everything else in the app
 * only sees the RideMatchingStrategy and FareStrategy interfaces.
 */
@Configuration
public class StrategyConfig {

    @Bean
    public RideMatchingStrategy rideMatchingStrategy(
            @Value("${ridewise.matching-strategy:nearest}") String name) {
        return switch (name.trim().toLowerCase()) {
            case "nearest" -> new NearestDriverStrategy();
            case "least-active" -> new LeastActiveDriverStrategy();
            default -> throw new IllegalArgumentException(
                    "Unknown ridewise.matching-strategy '" + name + "' (use nearest or least-active)");
        };
    }

    @Bean
    public FareStrategy fareStrategy(
            @Value("${ridewise.fare-strategy:default}") String name,
            @Value("${ridewise.peak-multiplier:1.5}") double peakMultiplier) {
        return switch (name.trim().toLowerCase()) {
            case "default" -> new DefaultFareStrategy();
            case "peak" -> new PeakHourFareStrategy(new DefaultFareStrategy(), peakMultiplier);
            default -> throw new IllegalArgumentException(
                    "Unknown ridewise.fare-strategy '" + name + "' (use default or peak)");
        };
    }
}
