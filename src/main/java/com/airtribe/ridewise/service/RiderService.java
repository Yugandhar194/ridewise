package com.airtribe.ridewise.service;

import com.airtribe.ridewise.exception.ResourceNotFoundException;
import com.airtribe.ridewise.model.Location;
import com.airtribe.ridewise.model.Rider;
import com.airtribe.ridewise.util.IdGenerator;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RiderService {

    private final IdGenerator idGenerator;
    private final Map<Integer, Rider> riders = new LinkedHashMap<>();

    public RiderService(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    public Rider registerRider(String name, Location location) {
        Rider rider = new Rider(idGenerator.next("rider"), name, location);
        riders.put(rider.getId(), rider);
        return rider;
    }

    public Rider getRiderById(int id) {
        Rider rider = riders.get(id);
        if (rider == null) {
            throw new ResourceNotFoundException("Rider", id);
        }
        return rider;
    }

    public List<Rider> getAllRiders() {
        return List.copyOf(riders.values());
    }
}
