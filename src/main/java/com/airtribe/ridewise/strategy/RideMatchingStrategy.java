package com.airtribe.ridewise.strategy;

import com.airtribe.ridewise.model.Driver;
import com.airtribe.ridewise.model.Rider;

import java.util.List;

public interface RideMatchingStrategy {

    /**
     * Picks one driver from the given list for this rider.
     *
     * @throws com.airtribe.ridewise.exception.NoDriverAvailableException if the list is empty
     */
    Driver findDriver(Rider rider, List<Driver> drivers);
}
