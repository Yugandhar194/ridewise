package com.airtribe.ridewise.service;

import com.airtribe.ridewise.exception.ResourceNotFoundException;
import com.airtribe.ridewise.model.Location;
import com.airtribe.ridewise.model.Rider;
import com.airtribe.ridewise.util.IdGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class RiderServiceTest {

    private RiderService riderService;

    @BeforeEach
    void setUp() {
        riderService = new RiderService(new IdGenerator());
    }

    @Test
    void registersRidersWithIncreasingIds() {
        Rider first = riderService.registerRider("Asha", new Location(0, 0));
        Rider second = riderService.registerRider("Kabir", new Location(2, 3));

        assertEquals(1, first.getId());
        assertEquals(2, second.getId());
        assertEquals(2, riderService.getAllRiders().size());
    }

    @Test
    void findsRiderById() {
        Rider rider = riderService.registerRider("Asha", new Location(0, 0));

        assertSame(rider, riderService.getRiderById(rider.getId()));
    }

    @Test
    void unknownRiderThrows() {
        assertThrows(ResourceNotFoundException.class, () -> riderService.getRiderById(99));
    }

    @Test
    void rejectsBlankNameAndMissingLocation() {
        assertThrows(IllegalArgumentException.class, () -> riderService.registerRider("  ", new Location(0, 0)));
        assertThrows(IllegalArgumentException.class, () -> riderService.registerRider("Asha", null));
    }

    @Test
    void failedRegistrationDoesNotStoreAnything() {
        assertThrows(IllegalArgumentException.class, () -> riderService.registerRider("", new Location(0, 0)));

        assertEquals(0, riderService.getAllRiders().size());
    }
}
