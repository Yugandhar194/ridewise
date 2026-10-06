package com.airtribe.ridewise.exception;

public class ResourceNotFoundException extends RideWiseException {

    public ResourceNotFoundException(String entity, int id) {
        super(entity + " with id " + id + " was not found");
    }
}
