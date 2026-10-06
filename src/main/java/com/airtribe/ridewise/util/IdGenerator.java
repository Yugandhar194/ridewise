package com.airtribe.ridewise.util;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/** Hands out 1, 2, 3... separately for each kind of entity, so riders, drivers and rides each start at 1. */
@Component
public class IdGenerator {

    private final Map<String, Integer> lastIds = new HashMap<>();

    public int next(String entity) {
        // merge() adds 1 to the stored value (or stores 1 the first time) and returns the new value
        return lastIds.merge(entity, 1, Integer::sum);
    }
}
