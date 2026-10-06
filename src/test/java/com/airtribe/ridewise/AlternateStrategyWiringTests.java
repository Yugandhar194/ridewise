package com.airtribe.ridewise;

import com.airtribe.ridewise.strategy.FareStrategy;
import com.airtribe.ridewise.strategy.LeastActiveDriverStrategy;
import com.airtribe.ridewise.strategy.PeakHourFareStrategy;
import com.airtribe.ridewise.strategy.RideMatchingStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/** Changing two properties swaps the strategies. No service or model class is touched. */
@SpringBootTest(properties = {
        "ridewise.console.enabled=false",
        "ridewise.matching-strategy=least-active",
        "ridewise.fare-strategy=peak"
})
class AlternateStrategyWiringTests {

    @Autowired
    private RideMatchingStrategy matchingStrategy;

    @Autowired
    private FareStrategy fareStrategy;

    @Test
    void propertiesSelectTheAlternateStrategies() {
        assertInstanceOf(LeastActiveDriverStrategy.class, matchingStrategy);
        assertInstanceOf(PeakHourFareStrategy.class, fareStrategy);
    }
}
