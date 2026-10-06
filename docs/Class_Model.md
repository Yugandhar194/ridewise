# RideWise: Class Model

This diagram shows the main classes in the project and how they relate to each other.

```mermaid
classDiagram
    direction LR

    class Rider {
        -int id
        -String name
        -Location location
    }

    class Driver {
        -int id
        -String name
        -VehicleType vehicleType
        -Location currentLocation
        -boolean available
        -int tripCount
        +distanceFrom(Location) double
        +acceptRide()
        +finishRide()
    }

    class Ride {
        -int id
        -Rider rider
        -double distance
        -Driver driver
        -RideStatus status
        -FareReceipt fareReceipt
        +assignDriver(Driver)
        +complete(FareReceipt)
        +cancel()
        +getVehicleType() VehicleType
    }

    class FareReceipt {
        <<record>>
        int rideId
        double amount
        LocalDateTime generatedAt
    }

    class Location {
        <<record>>
        double x
        double y
        +distanceTo(Location) double
    }

    class RideStatus {
        <<enumeration>>
        REQUESTED
        ASSIGNED
        COMPLETED
        CANCELLED
    }

    class VehicleType {
        <<enumeration>>
        BIKE
        AUTO
        CAR
    }

    class RideMatchingStrategy {
        <<interface>>
        +findDriver(Rider, List~Driver~) Driver
    }

    class NearestDriverStrategy
    class LeastActiveDriverStrategy

    class FareStrategy {
        <<interface>>
        +calculateFare(Ride) double
    }

    class DefaultFareStrategy
    class PeakHourFareStrategy

    class RiderService {
        +registerRider(String, Location) Rider
        +getRiderById(int) Rider
        +getAllRiders() List~Rider~
    }
    class DriverService {
        +registerDriver(String, Location, VehicleType) Driver
        +getDriverById(int) Driver
        +updateAvailability(int, boolean)
        +getAvailableDrivers() List~Driver~
        +getAllDrivers() List~Driver~
    }
    class RideService {
        +requestRide(int, double) Ride
        +completeRide(int) Ride
        +cancelRide(int) Ride
        +getRideById(int) Ride
        +getAllRides() List~Ride~
    }
    class IdGenerator
    class StrategyConfig

    Rider "1" --> "0..*" Ride : requests
    Driver "1" --> "0..*" Ride : serves
    Ride "1" *-- "0..1" FareReceipt
    Ride --> RideStatus
    Driver --> VehicleType
    Rider --> Location
    Driver --> Location

    RideMatchingStrategy <|.. NearestDriverStrategy
    RideMatchingStrategy <|.. LeastActiveDriverStrategy
    FareStrategy <|.. DefaultFareStrategy
    FareStrategy <|.. PeakHourFareStrategy
    PeakHourFareStrategy o-- FareStrategy : wraps

    RideService --> RiderService
    RideService --> DriverService
    RideService --> RideMatchingStrategy
    RideService --> FareStrategy
    RideService --> IdGenerator
    RiderService --> IdGenerator
    DriverService --> IdGenerator
    StrategyConfig ..> RideMatchingStrategy : creates
    StrategyConfig ..> FareStrategy : creates
```

## Layers in the project

| Layer | Package | Responsibility |
|-------|---------|----------------|
| UI | `Main` and `Main.Menu` | Read input, call services, show results |
| Service | `service` | Core use cases such as register, list, request, complete, and cancel |
| Strategy | `strategy` | Driver matching and fare calculation logic |
| Model | `model` | Domain objects and rules like ride lifecycle and driver availability |
| Config | `config` | Selects which strategy implementations to use |
| Support | `exception`, `util` | Exceptions and ID generation |

The main direction of dependency is simple:

UI -> Service -> Strategy / Model

The model does not depend on services. This keeps the code easier to test and easier to extend.
