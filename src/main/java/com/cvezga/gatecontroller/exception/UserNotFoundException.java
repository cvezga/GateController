package com.cvezga.gatecontroller.exception;

/**
 * Indicates that no user exists for a requested database identifier.
 */
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("User not found with ID: " + id);
    }
}
