# RideWise: Requirements

RideWise is a console-based ride-sharing system. It is not meant to be a full production app. Instead, it is a clean low-level design that demonstrates the Strategy pattern, composition over inheritance, SOLID principles, low coupling, high cohesion, and the Law of Demeter.

## Functional requirements

| # | Requirement | Where it is implemented |
|---|-------------|-------------------------|
| 1 | Register riders | `RiderService.registerRider` |
| 2 | Register drivers | `DriverService.registerDriver` |
| 3 | Show available drivers | `DriverService.getAvailableDrivers` |
| 4 | Request a ride | `RideService.requestRide` |
| 5 | Match a ride to a driver using a strategy | `RideMatchingStrategy` (`NearestDriverStrategy`, `LeastActiveDriverStrategy`) |
| 6 | Calculate fare using a pricing strategy | `FareStrategy` (`DefaultFareStrategy`, `PeakHourFareStrategy`) |
| 7 | Track ride status: REQUESTED, ASSIGNED, COMPLETED, CANCELLED | `Ride` lifecycle and `RideStatus` |

There is no separate `FareCalculator` class. Fare calculation is the responsibility of the classes that implement `FareStrategy`.

## Service operations

| Service | Operations |
|---------|------------|
| `RiderService` | Register a rider, get a rider by ID, list all riders |
| `DriverService` | Register a driver, get a driver by ID, update availability, list available drivers, list all drivers |
| `RideService` | Request, complete, or cancel a ride; get a ride by ID; list all rides |

When a ride is requested, `RideService` asks `RideMatchingStrategy` to choose an available driver. The fare is calculated by `FareStrategy` when the ride is completed.

The console menu in `Main.java` includes the following options:

1. Add Rider
2. Add Driver
3. View Available Drivers
4. Request Ride
5. Complete Ride
6. View Rides
7. Exit

Every menu action goes through the service layer. Invalid input is caught and shown to the user without crashing the application.

## Non-functional requirements

| Requirement | How it is met |
|-------------|---------------|
| Easy to extend pricing logic | Add a new class implementing `FareStrategy` and a new case in `StrategyConfig` |
| Easy to change driver matching logic | Add a new class implementing `RideMatchingStrategy` and a new case in `StrategyConfig`, or change the property in `application.properties` |
| Low coupling between services | `RideService` depends on `RiderService`, `DriverService`, and interfaces rather than concrete strategy classes |
| Maintainable and readable code | Small classes, single responsibility, and tests for each layer |

## Assumptions

- Locations are represented as points on a flat map. Distance is calculated in a straight line, and one unit is treated as one kilometre.
- A ride is matched to a driver when the ride is requested. If no driver is available, the request fails with `NoDriverAvailableException`, and no ride is stored. This means `REQUESTED` is only a short-lived state.
- The distance for a trip is entered by the user when the ride is requested.
- The fare is calculated when the ride is completed, and the receipt is stored on the ride itself.
- Data is kept in memory only, since this project is a small MVP and does not need a database.
- `VehicleType` is stored on the driver and affects the per-kilometre rate in `DefaultFareStrategy`.
- “Least active” means the driver with the fewest rides accepted so far. The trip count increases when a driver is assigned a ride, including rides that are later cancelled.
- Cancelling is supported through `RideService.cancelRide`, so `CANCELLED` is a valid ride status. The console menu does not currently offer a cancel option; cancellation is available through the service layer only.
