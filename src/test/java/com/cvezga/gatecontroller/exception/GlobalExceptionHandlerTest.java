package com.cvezga.gatecontroller.exception;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for REST error translation in {@link GlobalExceptionHandler}.
 */
class GlobalExceptionHandlerTest {

    @Test
    void userNotFoundProducesNotFoundResponse() {
        LocalDateTime before = LocalDateTime.now();

        var response = new GlobalExceptionHandler()
                .handleUserNotFound(new UserNotFoundException(42L));

        assertThat(response.getStatusCode().value()).isEqualTo(404);
        assertThat(response.getBody()).containsEntry("status", 404)
                .containsEntry("error", "Not Found")
                .containsEntry("message", "User not found with ID: 42");
        assertThat(response.getBody().get("timestamp"))
                .isInstanceOf(LocalDateTime.class)
                .satisfies(value -> assertThat((LocalDateTime) value)
                        .isBetween(before, LocalDateTime.now()));
    }
}
