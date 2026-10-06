package com.airtribe.ridewise;

import com.airtribe.ridewise.service.DriverService;
import com.airtribe.ridewise.service.RideService;
import com.airtribe.ridewise.service.RiderService;
import com.airtribe.ridewise.strategy.DefaultFareStrategy;
import com.airtribe.ridewise.strategy.NearestDriverStrategy;
import com.airtribe.ridewise.util.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Drives the real menu with scripted keyboard input and checks what it prints. */
class MainMenuTest {

    private RiderService riderService;
    private DriverService driverService;
    private RideService rideService;

    @BeforeEach
    void setUp() {
        IdGenerator ids = new IdGenerator();
        riderService = new RiderService(ids);
        driverService = new DriverService(ids);
        rideService = new RideService(riderService, driverService,
                new NearestDriverStrategy(), new DefaultFareStrategy(), ids);
    }

    private String runWith(String... lines) {
        String input = String.join("\n", lines) + "\n";
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        new Main.Menu(new Scanner(input), new PrintStream(buffer), riderService, driverService, rideService).run();
        return buffer.toString();
    }

    private int count(String text, String part) {
        return text.split(java.util.regex.Pattern.quote(part), -1).length - 1;
    }

    @Test
    void fullRideFlowWorksThroughTheMenu() {
        String output = runWith(
                "1", "Asha", "0", "0",             // add rider
                "2", "Ravi", "car", "1", "1",      // add driver (vehicle type is case-insensitive)
                "3",                               // view available drivers
                "4", "1", "5",                     // request a 5 km ride for rider 1
                "5", "1",                          // complete ride 1
                "6",                               // view rides
                "7");                              // exit

        assertTrue(output.contains("Registered: Rider #1 Asha"));
        assertTrue(output.contains("Registered: Driver #1 Ravi (CAR)"));
        assertTrue(output.contains("Ride booked: Ride #1 [ASSIGNED] rider=Asha, driver=Ravi"));
        assertTrue(output.contains("Ride completed: Ride #1 [COMPLETED]"));
        assertTrue(output.contains("fare=120.0"));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    void invalidMenuChoiceIsReportedAndMenuContinues() {
        String output = runWith("abc", "9", "7");

        assertEquals(2, count(output, "Please choose a number from 1 to 7."));
        assertTrue(output.contains("Goodbye!"));
    }

    @Test
    void invalidNumbersAreCaughtWithoutCrashing() {
        String output = runWith(
                "1", "Asha", "north", "0",         // x is not a number
                "4", "abc", "5",                   // rider id is not a number
                "7");

        assertTrue(output.contains("Error: 'north' is not a valid number"));
        assertTrue(output.contains("Error: 'abc' is not a valid whole number"));
        assertEquals(0, riderService.getAllRiders().size());
    }

    @Test
    void invalidVehicleTypeIsRejected() {
        String output = runWith("2", "Ravi", "helicopter", "1", "1", "7");

        assertTrue(output.contains("Error: 'helicopter' is not a vehicle type"));
        assertEquals(0, driverService.getAllDrivers().size());
    }

    @Test
    void requestingRideWithoutDriversShowsFriendlyError() {
        String output = runWith("1", "Asha", "0", "0", "4", "1", "5", "7");

        assertTrue(output.contains("Error: No driver is available right now"));
        assertEquals(0, rideService.getAllRides().size());
    }

    @Test
    void unknownIdsAndBadStatesAreReported() {
        String output = runWith(
                "4", "5", "3",                     // rider 5 does not exist
                "5", "8",                          // ride 8 does not exist
                "7");

        assertTrue(output.contains("Error: Rider with id 5 was not found"));
        assertTrue(output.contains("Error: Ride with id 8 was not found"));
    }

    @Test
    void emptyListsHaveFriendlyMessages() {
        String output = runWith("3", "6", "7");

        assertTrue(output.contains("No drivers are available right now."));
        assertTrue(output.contains("No rides yet."));
    }

    @Test
    void endOfInputExitsCleanly() {
        String output = runWith("1", "Asha");     // input stops in the middle of "Add Rider"

        assertTrue(output.contains("Input closed. Exiting."));
    }
}
