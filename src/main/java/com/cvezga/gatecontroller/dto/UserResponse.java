package com.cvezga.gatecontroller.dto;

/**
 * Read-only representation of user data that excludes the password.
 *
 * @param id database identifier
 * @param username login name
 * @param email email address
 * @param firstName given name
 * @param lastName family name
 * @param role authorization role
 */
public record UserResponse(
        Long id,
        String username,
        String email,
        String firstName,
        String lastName,
        String role
) {}
