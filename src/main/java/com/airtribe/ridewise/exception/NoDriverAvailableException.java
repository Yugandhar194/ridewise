package com.airtribe.ridewise.exception;

public class NoDriverAvailableException extends RideWiseException {

    public NoDriverAvailableException() {
        super("No driver is available right now. Please try again later.");
    }
}
