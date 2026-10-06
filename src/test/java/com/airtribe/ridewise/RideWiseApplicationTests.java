package com.airtribe.ridewise;

import com.airtribe.ridewise.service.RideService;
import com.airtribe.ridewise.strategy.DefaultFareStrategy;
import com.airtribe.ridewise.strategy.FareStrategy;
import com.airtribe.ridewise.strategy.NearestDriverStrategy;
import com.airtribe.ridewise.strategy.RideMatchingStrategy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/** Loads the real Spring context with the default application.properties (menu switched off). */
@SpringBootTest(properties = "ridewise.console.enabled=false")
class RideWiseApplicationTests {

    @Autowired
    private ApplicationContext context;

    @Autowired
    private RideMatchingStrategy matchingStrategy;

    @Autowired
    private FareStrategy fareStrategy;

    @Test
    void contextLoadsAndWiresTheServices() {
        assertEquals(1, context.getBeansOfType(RideService.class).size());
    }

    @Test
    void defaultStrategiesAreNearestDriverAndDefaultFare() {
        assertInstanceOf(NearestDriverStrategy.class, matchingStrategy);
        assertInstanceOf(DefaultFareStrategy.class, fareStrategy);
    }

    @Test
    void consoleRunnerIsNotCreatedWhenDisabled() {
        assertEquals(0, context.getBeansOfType(org.springframework.boot.CommandLineRunner.class).size());
    }
}
