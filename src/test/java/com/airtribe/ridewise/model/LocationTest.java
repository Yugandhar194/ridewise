package com.airtribe.ridewise.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LocationTest {

    @Test
    void distanceIsStraightLine() {
        assertEquals(5.0, new Location(0, 0).distanceTo(new Location(3, 4)), 0.0001);
    }

    @Test
    void rejectsNonNumericCoordinates() {
        assertThrows(IllegalArgumentException.class, () -> new Location(Double.NaN, 1));
        assertThrows(IllegalArgumentException.class, () -> new Location(1, Double.POSITIVE_INFINITY));
    }
}
