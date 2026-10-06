# RideWise

RideWise is a console-based ride-sharing system.
The project focuses on the Strategy pattern, SOLID principles, composition over inheritance, low coupling, and the Law of Demeter.

## Features

- Register riders and drivers
- Allow each driver to have a vehicle type: BIKE, AUTO, or CAR
- View available drivers
- Request a ride and assign it to a driver using a pluggable strategy
- Choose between nearest-driver matching and least-active-driver matching
- Least-active matching selects the driver with the fewest rides accepted so far; cancelled rides count toward this total
- Calculate fare using a pluggable pricing strategy
- Support default pricing and peak-hour pricing
- Track ride states such as REQUESTED, ASSIGNED, COMPLETED, and CANCELLED
- Handle invalid input gracefully without crashing the console app

## Requirements

- JDK 17 or newer
- Maven 3.8 or newer

## Run the application

```bash
mvn spring-boot:run
```

If you want to build the jar first, use:

```bash
mvn clean package
java -jar target/ridewise-1.0.0.jar
```

## Run tests

```bash
mvn test
```

## Configuration

File: `src/main/resources/application.properties`

| Property | Possible values | Default |
|----------|-----------------|---------|
| `ridewise.matching-strategy` | `nearest`, `least-active` | `nearest` |
| `ridewise.fare-strategy` | `default`, `peak` | `default` |
| `ridewise.peak-multiplier` | number greater than or equal to 1 | `1.5` |
| `ridewise.console.enabled` | `true`, `false` | `true` |

You can override these values from the command line, for example:

```bash
java -jar target/ridewise-1.0.0.jar --ridewise.matching-strategy=least-active --ridewise.fare-strategy=peak
```

## Fare rules

- Default fare: base charge of 30 plus per-kilometre rate based on vehicle type
  - BIKE: 8
  - AUTO: 12
  - CAR: 18
- Peak fare: default fare multiplied by the configured peak multiplier

Example: a 5 km CAR ride normally costs 30 + (18 x 5) = 120.0. At peak time, with multiplier 1.5, it becomes 180.0.

## Sample session

```text
===== RideWise =====
1. Add Rider
2. Add Driver
3. View Available Drivers
4. Request Ride
5. Complete Ride
6. View Rides
7. Exit
Choose an option: 1
Rider name: Asha
Location x: 0
Location y: 0
Registered: Rider #1 Asha at (0.0, 0.0)
...
Choose an option: 4
Rider id: 1
Trip distance in km: 5
Ride booked: Ride #1 [ASSIGNED] rider=Asha, driver=Ravi, distance=5.0 km
...
Choose an option: 5
Ride id: 1
Ride completed: Ride #1 [COMPLETED] rider=Asha, driver=Ravi, distance=5.0 km, fare=120.0
```

## Project structure

```text
ridewise/
├── pom.xml
├── README.md
├── docs/
│   ├── Requirements.md
│   ├── Class_Model.md
│   ├── SOLID_Reflection.md
│   ├── Object_Relationships.md
│   └── Code_Walkthrough.md
└── src/
    ├── main/
  │   ├── java/com/airtribe/ridewise/
  │   │   ├── Main.java
  │   │   ├── config/
  │   │   │   └── StrategyConfig.java
  │   │   ├── exception/
  │   │   │   ├── InvalidRideStateException.java
  │   │   │   ├── NoDriverAvailableException.java
  │   │   │   ├── ResourceNotFoundException.java
  │   │   │   └── RideWiseException.java
  │   │   ├── model/
  │   │   │   ├── Driver.java
  │   │   │   ├── FareReceipt.java
  │   │   │   ├── Location.java
  │   │   │   ├── Ride.java
  │   │   │   ├── Rider.java
  │   │   │   ├── RideStatus.java
  │   │   │   └── VehicleType.java
  │   │   ├── service/
  │   │   │   ├── DriverService.java
  │   │   │   ├── RiderService.java
  │   │   │   └── RideService.java
  │   │   ├── strategy/
  │   │   │   ├── DefaultFareStrategy.java
  │   │   │   ├── FareStrategy.java
  │   │   │   ├── LeastActiveDriverStrategy.java
  │   │   │   ├── NearestDriverStrategy.java
  │   │   │   ├── PeakHourFareStrategy.java
  │   │   │   └── RideMatchingStrategy.java
  │   │   └── util/
  │   │       └── IdGenerator.java
  │   └── resources/
  │       └── application.properties
  └── test/java/com/airtribe/ridewise/
    ├── AlternateStrategyWiringTests.java
    ├── MainMenuTest.java
    ├── RideWiseApplicationTests.java
    ├── model/
    │   ├── LocationTest.java
    │   └── RideTest.java
    ├── service/
    │   ├── DriverServiceTest.java
    │   ├── RiderServiceTest.java
    │   └── RideServiceTest.java
    └── strategy/
      ├── FareStrategyTest.java
      └── MatchingStrategyTest.java
```

## Adding a new strategy

1. Create a new class that implements `RideMatchingStrategy` or `FareStrategy`.
2. Add a matching case in `StrategyConfig`.
3. Update the app property in `application.properties`.

No other class needs to change for a simple strategy addition.

## Documentation

The `docs/` folder contains the requirements, class design, object relationships, SOLID reflection, and a guided walkthrough of the code.
