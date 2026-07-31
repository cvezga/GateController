package com.cvezga.gatecontroller.controller;

import com.cvezga.gatecontroller.entity.Config;
import com.cvezga.gatecontroller.entity.Event;
import com.cvezga.gatecontroller.service.ConfigService;
import com.cvezga.gatecontroller.service.EventService;
import com.cvezga.gatecontroller.service.MqttPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.ui.ExtendedModelMap;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for gate-command validation, publishing, and event-page behavior
 * in {@link ButtonController}.
 */
class ButtonControllerTest {

    private MqttPublisher publisher;
    private ConfigService configService;
    private EventService eventService;
    private Authentication authentication;
    private ButtonController controller;

    @BeforeEach
    void setUp() {
        publisher = mock(MqttPublisher.class);
        configService = mock(ConfigService.class);
        eventService = mock(EventService.class);
        authentication = mock(Authentication.class);
        when(authentication.getName()).thenReturn("alice");
        controller = new ButtonController(publisher, configService, eventService);
    }

    @Test
    void buttonPageAddsAuthenticatedUsername() {
        ExtendedModelMap model = new ExtendedModelMap();

        assertThat(controller.buttonPage(authentication, model)).isEqualTo("button");
        assertThat(model).containsEntry("username", "alice");
    }

    @Test
    void eventsPageAddsUsernameAndOrderedEvents() {
        List<Event> events = List.of(new Event());
        when(eventService.findAllNewestFirst()).thenReturn(events);
        ExtendedModelMap model = new ExtendedModelMap();

        assertThat(controller.eventsPage(authentication, model)).isEqualTo("events");
        assertThat(model).containsEntry("username", "alice").containsEntry("events", events);
    }

    @Test
    void sendCommandReportsMissingConfiguration() {
        when(configService.find()).thenReturn(Optional.empty());
        ExtendedModelMap model = new ExtendedModelMap();

        assertThat(controller.sendCommand(10, -84, 3, authentication, model)).isEqualTo("button");
        assertThat(model).containsEntry("username", "alice").containsEntry("message", "No config found");
        verify(publisher, never()).publish();
        verifyEventWasSaved();
    }

    @Test
    void sendCommandRejectsLocationOutsideAllowedDistance() {
        when(configService.find()).thenReturn(Optional.of(config(10, -84, 10)));
        ExtendedModelMap model = new ExtendedModelMap();

        controller.sendCommand(11, -84, 3, authentication, model);

        assertThat(model).containsEntry("message", "ERROR: You must be within 10 meters of the gate");
        verify(publisher, never()).publish();
    }

    @Test
    void sendCommandReportsSuccessfulPublish() {
        when(configService.find()).thenReturn(Optional.of(config(10, -84, 10)));
        when(publisher.publish()).thenReturn(true);
        ExtendedModelMap model = new ExtendedModelMap();

        controller.sendCommand(10, -84, 3, authentication, model);

        assertThat(model).containsEntry("message", "Command published successfully");
    }

    @Test
    void sendCommandReportsPublishFailure() {
        when(configService.find()).thenReturn(Optional.of(config(10, -84, 10)));
        when(publisher.publish()).thenReturn(false);
        ExtendedModelMap model = new ExtendedModelMap();

        controller.sendCommand(10, -84, 3, authentication, model);

        assertThat(model).containsEntry("message", "ERROR: Command NOT SEND!");
    }

    @Test
    void sendCommandConvertsUnexpectedExceptionToServerError() {
        when(configService.find()).thenThrow(new IllegalStateException("failure"));
        ExtendedModelMap model = new ExtendedModelMap();

        controller.sendCommand(10, -84, 3, authentication, model);

        assertThat(model).containsEntry("message", "ERROR: Server error");
    }

    private Config config(double latitude, double longitude, int maximumDistance) {
        Config config = new Config();
        config.setGateLatitude(latitude);
        config.setGateLongitude(longitude);
        config.setGateMaxDistanceMeters(maximumDistance);
        return config;
    }

    private void verifyEventWasSaved() {
        verify(eventService).saveEvent(
                eq("alice"),
                eq("button"),
                contains("sendCommand from latitude=10.0, longitude=-84.0")
        );
    }
}
