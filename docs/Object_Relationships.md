# RideWise: Object Relationships

This document explains how the main objects in the system relate to each other.

| Relationship | Type | Explanation |
|--------------|------|-------------|
| Rider -> Ride | Association | A rider can request many rides. A ride holds a rider, but the rider still exists independently. |
| Driver -> Ride | Association | A driver can serve many rides over time. A ride holds a driver after assignment. |
| Ride -> FareReceipt | Composition | A receipt is created specifically for one ride and has no meaning without it. |
| RideService -> Strategies | Composition | `RideService` keeps strategy objects as fields and delegates work to them. This is the project’s use of composition over inheritance. |
| PeakHourFareStrategy -> FareStrategy | Decorator-style composition | Peak-hour pricing wraps another fare strategy instead of extending a base class. |
| RideService -> RiderService, DriverService | Dependency injection | The services are passed in through the constructor and used together. |
| Rider / Driver -> Location | Composition | `Location` is a small value object that belongs to the rider or driver. |
| Concrete strategy classes -> interfaces | Realization | Classes like `NearestDriverStrategy` and `DefaultFareStrategy` implement the strategy interfaces. |

## A note about the strategy relationship

In strict UML, objects injected from outside the class are usually described as aggregation because they can be reused and are not necessarily owned by the service. However, in design terms, this project is using composition because the service has the behaviour as a field rather than inheriting it.

For a classroom explanation, the project’s wording is acceptable: the service “has” a matching strategy and a fare strategy.

## Law of Demeter in practice

| Instead of | The code does | Why it is better |
|------------|---------------|------------------|
| `driver.getCurrentLocation().distanceTo(rider.getLocation())` in a strategy | `driver.distanceFrom(rider.getLocation())` | The strategy does not need to know how the driver stores its location. |
| `ride.getDriver().getVehicleType()` inside fare logic | `ride.getVehicleType()` | The fare logic does not need to reach into the driver object. |
| `ride.getDriver().finishRide()` inside `RideService` | `ride.complete(receipt)` | The service asks the ride to complete itself instead of reaching through it. |

This is a small but important example of keeping object interaction simple and clean.
