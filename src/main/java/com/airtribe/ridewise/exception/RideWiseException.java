package com.airtribe.ridewise.exception;

/** Base type for every business-rule failure in RideWise, so callers can catch them in one place. */
public class RideWiseException extends RuntimeException {

    public RideWiseException(String message) {
        super(message);
    }
}
