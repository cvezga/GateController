package com.cvezga.gatecontroller.controller;

import com.cvezga.gatecontroller.entity.CommandValidation;
import com.cvezga.gatecontroller.service.CommandValidationService;
import com.cvezga.gatecontroller.service.GateNotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class CommandConfirmationControllerTest {

    private CommandValidationService service;
    private Authentication authentication;
    private GateNotificationService notificationService;
    private CommandConfirmationController controller;
    private CommandValidation validation;

    @BeforeEach
    void setUp() {
        service = mock(CommandValidationService.class);
        authentication = mock(Authentication.class);
        notificationService = mock(GateNotificationService.class);
        when(authentication.getName()).thenReturn("alice");

        validation = new CommandValidation();
        validation.setId(UUID.randomUUID());
        validation.setUsername("alice");
        validation.setRandomInt(12345);
        validation.setConfirmationNumber(7);
        when(service.findById(validation.getId())).thenReturn(Optional.of(validation));

        controller = new CommandConfirmationController(service, notificationService);
    }

    @Test
    void displaysConfirmationForItsAuthenticatedUser() {
        ExtendedModelMap model = new ExtendedModelMap();

        assertThat(controller.confirmationPage(validation.getId(), authentication, model))
                .isEqualTo("command-confirmation");
        assertThat(model).containsEntry("username", "alice")
                .containsEntry("commandValidation", validation);
    }

    @Test
    void acceptsAZeroPaddedTwoDigitConfirmation() {
        ExtendedModelMap model = new ExtendedModelMap();

        controller.confirm(validation.getId(), 12345,  7, authentication, model);

        assertThat(validation.getStatus()).isEqualTo("confirmed");
        assertThat(model).containsEntry("message", "Command confirmed successfully");
        verify(service).save(validation);
    }

    @Test
    void rejectsValuesThatDoNotContainExactlyTwoDigits() {
        ExtendedModelMap model = new ExtendedModelMap();

        controller.confirm(validation.getId(), 12345,  7, authentication, model);

        assertThat(model).containsEntry("message", "ERROR: Enter exactly two digits");
        verify(service, never()).save(any());
    }

    @Test
    void rejectsTamperedHiddenValues() {
        assertThatThrownBy(() -> controller.confirm(
                validation.getId(), 99999,  7, authentication, new ExtendedModelMap()))
                .isInstanceOf(ResponseStatusException.class);
        verify(service, never()).save(any());
    }

    @Test
    void hidesAnotherUsersCommand() {
        validation.setUsername("bob");

        assertThatThrownBy(() -> controller.confirmationPage(
                validation.getId(), authentication, new ExtendedModelMap()))
                .isInstanceOf(ResponseStatusException.class);
    }
}
