package com.airtribe.ridewise.service;

import com.airtribe.ridewise.exception.ResourceNotFoundException;
import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.Location;
import com.airtribe.ridewise.model.VehicleType;
import com.airtribe.ridewise.util.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DriverServiceTest {

    private DriverService driverService;

    @BeforeEach
    void setUp() {
        driverService = new DriverService(new IdGenerator());
    }

    @Test
    void newDriversAreAvailable() {
        Driver driver = driverService.registerDriver("Ravi", new Location(1, 1), VehicleType.CAR);

        assertEquals(1, driver.getId());
        assertTrue(driver.isAvailable());
        assertEquals(1, driverService.getAvailableDrivers().size());
    }

    @Test
    void availableListSkipsUnavailableDrivers() {
        Driver ravi = driverService.registerDriver("Ravi", new Location(1, 1), VehicleType.CAR);
        Driver meera = driverService.registerDriver("Meera", new Location(2, 2), VehicleType.BIKE);

        driverService.updateAvailability(ravi.getId(), false);

        List<Driver> available = driverService.getAvailableDrivers();
        assertEquals(1, available.size());
        assertEquals(meera.getId(), available.get(0).getId());
        assertEquals(2, driverService.getAllDrivers().size());
    }

    @Test
    void availabilityCanBeTurnedBackOn() {
        Driver ravi = driverService.registerDriver("Ravi", new Location(1, 1), VehicleType.CAR);
        driverService.updateAvailability(ravi.getId(), false);
        assertFalse(ravi.isAvailable());

        driverService.updateAvailability(ravi.getId(), true);

        assertTrue(ravi.isAvailable());
    }

    @Test
    void unknownDriverThrows() {
        assertThrows(ResourceNotFoundException.class, () -> driverService.getDriverById(7));
        assertThrows(ResourceNotFoundException.class, () -> driverService.updateAvailability(7, true));
    }

    @Test
    void rejectsInvalidInput() {
        assertThrows(IllegalArgumentException.class,
                () -> driverService.registerDriver("", new Location(0, 0), VehicleType.CAR));
        assertThrows(IllegalArgumentException.class,
                () -> driverService.registerDriver("Ravi", null, VehicleType.CAR));
        assertThrows(IllegalArgumentException.class,
                () -> driverService.registerDriver("Ravi", new Location(0, 0), null));
    }
}
