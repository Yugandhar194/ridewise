package com.airtribe.ridewise;

import com.airtribe.ridewise.exception.RideWiseException;
import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.Location;
import com.airtribe.ridewise.model.Ride;
import com.airtribe.ridewise.model.VehicleType;
import com.airtribe.ridewise.service.DriverService;
import com.airtribe.ridewise.service.RideService;
import com.airtribe.ridewise.service.RiderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

import java.io.PrintStream;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

@SpringBootApplication
public class Main {

    public static void main(String[] args) {
        SpringApplication.run(Main.class, args);
    }

    /** Starts the menu once the Spring context is ready. Tests switch it off with ridewise.console.enabled=false. */
    @Bean
    @ConditionalOnProperty(name = "ridewise.console.enabled", havingValue = "true", matchIfMissing = true)
    CommandLineRunner consoleRunner(RiderService riderService, DriverService driverService, RideService rideService) {
        return args -> new Menu(new Scanner(System.in), System.out, riderService, driverService, rideService).run();
    }

    /** The console UI. It only talks to the service layer and never touches strategies or storage directly. */
    static class Menu {

        private final Scanner in;
        private final PrintStream out;
        private final RiderService riderService;
        private final DriverService driverService;
        private final RideService rideService;

        Menu(Scanner in, PrintStream out, RiderService riderService,
             DriverService driverService, RideService rideService) {
            this.in = in;
            this.out = out;
            this.riderService = riderService;
            this.driverService = driverService;
            this.rideService = rideService;
        }

        void run() {
            try {
                while (true) {
                    printMenu();
                    String choice = prompt("Choose an option: ");
                    if (choice.equals("7")) {
                        out.println("Goodbye!");
                        return;
                    }
                    handle(choice);
                }
            } catch (NoSuchElementException e) {
                // Scanner.nextLine() throws this when the input stream ends (Ctrl+D / Ctrl+Z, or a test script running out)
                out.println("Input closed. Exiting.");
            }
        }

        private void handle(String choice) {
            try {
                switch (choice) {
                    case "1" -> addRider();
                    case "2" -> addDriver();
                    case "3" -> viewAvailableDrivers();
                    case "4" -> requestRide();
                    case "5" -> completeRide();
                    case "6" -> viewRides();
                    default -> out.println("Please choose a number from 1 to 7.");
                }
            } catch (IllegalArgumentException | RideWiseException e) {
                out.println("Error: " + e.getMessage());
            }
        }

        private void printMenu() {
            out.println();
            out.println("===== RideWise =====");
            out.println("1. Add Rider");
            out.println("2. Add Driver");
            out.println("3. View Available Drivers");
            out.println("4. Request Ride");
            out.println("5. Complete Ride");
            out.println("6. View Rides");
            out.println("7. Exit");
        }

        private void addRider() {
            String name = prompt("Rider name: ");
            Location location = readLocation();
            out.println("Registered: " + riderService.registerRider(name, location));
        }

        private void addDriver() {
            String name = prompt("Driver name: ");
            VehicleType vehicleType = readVehicleType();
            Location location = readLocation();
            out.println("Registered: " + driverService.registerDriver(name, location, vehicleType));
        }

        private void viewAvailableDrivers() {
            List<Driver> available = driverService.getAvailableDrivers();
            if (available.isEmpty()) {
                out.println("No drivers are available right now.");
                return;
            }
            available.forEach(out::println);
        }

        private void requestRide() {
            int riderId = readInt("Rider id: ");
            double distance = readDouble("Trip distance in km: ");
            out.println("Ride booked: " + rideService.requestRide(riderId, distance));
        }

        private void completeRide() {
            int rideId = readInt("Ride id: ");
            out.println("Ride completed: " + rideService.completeRide(rideId));
        }

        private void viewRides() {
            List<Ride> rides = rideService.getAllRides();
            if (rides.isEmpty()) {
                out.println("No rides yet.");
                return;
            }
            rides.forEach(out::println);
        }

        // ----- input helpers: every bad value becomes an IllegalArgumentException that handle() reports -----

        private String prompt(String label) {
            out.print(label);
            out.flush();
            return in.nextLine().trim();
        }

        private int readInt(String label) {
            String text = prompt(label);
            try {
                return Integer.parseInt(text);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("'" + text + "' is not a valid whole number");
            }
        }

        private double readDouble(String label) {
            String text = prompt(label);
            try {
                return Double.parseDouble(text);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("'" + text + "' is not a valid number");
            }
        }

        private Location readLocation() {
            double x = readDouble("Location x: ");
            double y = readDouble("Location y: ");
            return new Location(x, y);
        }

        private VehicleType readVehicleType() {
            String text = prompt("Vehicle type (BIKE / AUTO / CAR): ");
            try {
                return VehicleType.valueOf(text.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("'" + text + "' is not a vehicle type. Choose BIKE, AUTO or CAR");
            }
        }
    }
}
