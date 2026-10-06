# RideWise: Code Walkthrough

This is a simple guide to the project, in the order that makes the most sense to read it.

## 1. `model/`: the core data and rules

### `Location` as a record

```java
public record Location(double x, double y) { ... }
```

A Java record is a short way to create an immutable value object. The compiler generates the constructor, getters like `x()` and `y()`, and methods like `equals()`, `hashCode()`, and `toString()`.

This is useful here because a location is just a coordinate. If two locations have the same `x` and `y`, they are considered the same location.

The class also validates values in a compact constructor:

```java
public Location {
    if (!Double.isFinite(x) || !Double.isFinite(y)) throw ...
}
```

This prevents invalid values such as `NaN` or infinity from entering the system.

The distance calculation uses `Math.hypot(dx, dy)`, which gives the straight-line distance in a safe way.

### `Rider` and `Driver`

The fields are marked `final` when they should never change. This helps keep object state consistent.

`Driver` is the main mutable entity. It keeps track of:

- `available`
- `tripCount`
- current location
- vehicle type

The important methods are:

- `distanceFrom(Location)`: tells the system how far the driver is from a point
- `acceptRide()`: marks the driver as busy and increases trip count
- `finishRide()`: marks the driver as available again

This is a good example of keeping logic near the data itself.

### `Ride`

`Ride` is the most important model class. It stores information about the rider, the assigned driver, the distance, the status, and the fare receipt.

The ride lifecycle is controlled through a small set of methods:

- `assignDriver(driver)`: only valid when the ride is in `REQUESTED` state
- `complete(receipt)`: only valid when the ride is in `ASSIGNED` state
- `cancel()`: valid from `REQUESTED` or `ASSIGNED`

The helper method `requireStatus(expected)` checks whether the ride is in the correct state. If not, it throws `InvalidRideStateException`.

This makes the ride behave like a tiny state machine. Illegal transitions are blocked automatically.

`getVehicleType()` delegates to the driver so fare logic does not need to reach into driver details directly.

### `FareReceipt`, `RideStatus`, and `VehicleType`

These are enum-style or record-style data types.

An enum is a fixed set of constant values. The compiler helps enforce valid values, and `switch` statements over enums are safer because missing cases can be caught earlier.

## 2. `strategy/`: replaceable behaviour

The strategy package contains the logic that can change without modifying the rest of the system.

### The interfaces

```java
public interface FareStrategy { double calculateFare(Ride ride); }
```

Each interface contains a single responsibility. The service depends on the interface, not an implementation.

### `NearestDriverStrategy`

This strategy finds the driver nearest to the rider.

```java
return drivers.stream()
        .min(Comparator.comparingDouble(driver -> driver.distanceFrom(rider.getLocation())))
        .orElseThrow(NoDriverAvailableException::new);
```

This code does a few useful things:

- `stream()` turns the list into a stream
- `Comparator.comparingDouble(...)` orders drivers by distance
- `min(...)` picks the closest driver
- `orElseThrow(...)` throws a useful exception if no driver is available

### `LeastActiveDriverStrategy`

This strategy chooses the driver with the fewest rides accepted so far. `tripCount` increases when a ride is assigned, so it also includes rides that are later cancelled. The strategy compares drivers using this count.

### `DefaultFareStrategy`

This strategy calculates the base fare. It checks the driver’s vehicle type and sets a different rate per kilometre.

```java
double ratePerKm = switch (ride.getVehicleType()) {
    case BIKE -> 8.0; case AUTO -> 12.0; case CAR -> 18.0;
};
```

Then it applies the formula:

`fare = 30 + ratePerKm * distance`

It rounds the result to two decimal places before returning it.

### `PeakHourFareStrategy`

This strategy wraps another `FareStrategy` and multiplies the fare by a chosen multiplier, such as 1.5. This follows the decorator pattern in a lightweight form.

## 3. `service/`: the use cases

### `RiderService` and `DriverService`

These services are annotated with `@Service`, which tells Spring to create and manage them as beans.

They use `LinkedHashMap` to preserve insertion order, so riders and drivers appear in the order they were registered.

`List.copyOf(...)` is used to return an unmodifiable list, which prevents accidental modification of the internal state.

### `RideService`

`RideService` is the main orchestrator. It receives the rider service, driver service, and strategy interfaces through the constructor.

The method `requestRide(riderId, distance)` works like this:

1. Validate the distance
2. Look up the rider
3. Create a new ride in `REQUESTED` state
4. Ask a matching strategy to choose a driver
5. Assign the driver to the ride
6. Save the ride

If step 4 fails because no driver is available, the ride is not stored. This avoids half-created rides.

`completeRide(rideId)` calculates the fare, creates a `FareReceipt`, and then calls `ride.complete(receipt)`, which updates the state and marks the driver free again.

`cancelRide(rideId)` delegates to `ride.cancel()`. This service operation is implemented and can set a ride to `CANCELLED`, but the console menu does not currently provide a cancel option.

## 4. `config/StrategyConfig`: Spring wiring

`StrategyConfig` is where Spring decides which concrete strategy classes to use.

```java
@Configuration
public class StrategyConfig {
    @Bean
    public RideMatchingStrategy rideMatchingStrategy(@Value("${ridewise.matching-strategy:nearest}") String name) { ... }
}
```

This class reads values from `application.properties` and creates the matching strategy and fare strategy beans.

This is a neat example of dependency injection: the service depends on interfaces, while Spring chooses the actual implementation.

## 5. `Main`: application startup and menu

`Main` is the entry point of the app.

- `@SpringBootApplication` informs Spring Boot to scan the project and set up the application
- `SpringApplication.run(...)` starts the Spring context
- `CommandLineRunner` starts the console menu after the application is ready

The menu reads input from a `Scanner`, then routes each option to the correct service.

The code catches invalid values and business errors and prints messages like `Error: ...` instead of crashing the entire program.

This makes the console app more user-friendly and easy to test.

## 6. `util/IdGenerator`

This small utility generates IDs for riders, drivers, and rides.

It keeps one counter per entity type, so each category starts from 1 and increments correctly.

## 7. What the tests prove

| File | What it checks |
|------|----------------|
| `RideTest` | Ride status changes and driver availability rules |
| `LocationTest` | Distance logic and invalid value handling |
| `MatchingStrategyTest` | Driver matching strategy behaviour |
| `FareStrategyTest` | Fare values, rounding, and multiplier effects |
| `RiderServiceTest` | Registration and validation for riders |
| `DriverServiceTest` | Registration and validation for drivers |
| `RideServiceTest` | The full ride lifecycle and business rules |
| `MainMenuTest` | Console menu interaction and bad-input handling |
| `RideWiseApplicationTests` | Spring app startup and wiring |
| `AlternateStrategyWiringTests` | Strategy selection through config |

## 8. Possible extensions

- Add a new driver-matching strategy such as `HighestRatedDriverStrategy`
- Add a cancel-ride option to the menu
- Replace the in-memory maps with a database-backed repository layer

The design is already flexible enough to support these changes without rewriting the core logic.
