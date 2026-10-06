# RideWise: SOLID and Design Principles Reflection

This project is built to show good software design in a small but realistic system. The main idea is that business rules are separated from the console interface, and behaviour can be changed without rewriting the whole application.

## SOLID principles

### Single Responsibility Principle (SRP)
Each service is responsible for one area of the domain.

- `RiderService` handles riders
- `DriverService` handles drivers
- `RideService` manages the ride lifecycle and use cases
- `FareStrategy` implementations handle fare calculation; the project does not use a separate `FareCalculator` class
- `RideMatchingStrategy` classes decide which driver should be assigned
- `IdGenerator` creates IDs
- `Main` handles user input and output
- `Ride` maintains its own lifecycle rules

This keeps responsibilities clear and makes the code easier to maintain.

### Open/Closed Principle (OCP)
The design is open for extension but closed for modification.

If we want a new driver matching strategy or a new fare strategy, we can add a new class and plug it in through `StrategyConfig`. We do not need to rewrite the service logic or the menu.

### Liskov Substitution Principle (LSP)
Every implementation of a strategy follows the same contract as the interface it implements.

- Every `RideMatchingStrategy` accepts a list of drivers and returns a driver, or throws `NoDriverAvailableException` if no driver is available.
- Every `FareStrategy` takes a ride and returns a numeric fare.

This means we can safely swap implementations without breaking the overall system.

### Interface Segregation Principle (ISP)
The strategy interfaces are small and focused.

- `RideMatchingStrategy` contains only the logic needed to select a driver.
- `FareStrategy` contains only the logic needed to calculate fare.

No class is forced to implement unrelated behaviour.

### Dependency Inversion Principle (DIP)
The service layer depends on abstractions, not concrete implementations.

`RideService` receives `RideMatchingStrategy` and `FareStrategy` through constructor injection instead of creating concrete strategy objects directly. Only `StrategyConfig` knows which implementations are active.

## Other design principles

### Composition over inheritance
There is no large inheritance hierarchy for behaviour. Instead, services and strategies are composed using interfaces.

For example, `PeakHourFareStrategy` wraps another `FareStrategy` and adjusts the fare instead of extending `DefaultFareStrategy`.

### DRY (Don't Repeat Yourself)
The logic for selecting a driver is written once in the matching strategy interface and implemented in specific strategy classes. `RideService` does not duplicate driver-selection logic.

The same principle applies to availability updates: `Driver.acceptRide()` and `Driver.finishRide()` handle that behaviour in one place.

### KISS (Keep It Simple, Stupid)
The classes are small and focused. The model is simple, the services are clear, and storage is in-memory only.

### YAGNI (You Ain't Gonna Need It)
The project deliberately avoids database access, payment systems, rating systems, and complex external services because they are not part of the assignment requirements.

### High cohesion and low coupling
Services communicate through method calls and constructor injection rather than direct dependency on each other’s implementation details. The ride lifecycle rules are kept inside `Ride`, which helps maintain cohesion.

### Law of Demeter
The code avoids deep chains of method calls. For example, the system uses methods like `driver.distanceFrom(...)` and `ride.getVehicleType()` rather than reaching through multiple objects in a messy way.

## Trade-offs and design choices

- `Ride` is aware of driver availability, because it calls `driver.acceptRide()` and `driver.finishRide()`. This keeps the ride lifecycle rule in one place.
- Strategies throw exceptions when no driver is available instead of returning null or Optional. This keeps the contract explicit and makes the service logic simpler.
- Fare values use `double` because the assignment expects a `double` return type. In a real production system, `BigDecimal` would usually be a better choice for money.
