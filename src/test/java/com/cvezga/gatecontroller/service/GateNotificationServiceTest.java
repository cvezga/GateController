package com.cvezga.gatecontroller.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class GateNotificationServiceTest {

    @Test
    void storesNotificationOnlyForTheTargetUser() {
        GateNotificationService service = service();

        service.handlePayload("{\"user\":\"alice\",\"number\":\"07\"}");

        assertThat(service.consumeForUser("bob")).isEmpty();
        assertThat(service.consumeForUser("alice")).contains("07");
        assertThat(service.consumeForUser("alice")).isEmpty();
    }

    @Test
    void padsAJsonNumericValueToTwoDigits() {
        GateNotificationService service = service();

        service.handlePayload("{\"user\":\"alice\",\"number\":7}");

        assertThat(service.consumeForUser("alice")).contains("07");
    }

    @Test
    void rejectsMalformedAndNonTwoDigitNotifications() {
        GateNotificationService service = service();

        service.handlePayload("not-json");
        service.handlePayload("{\"user\":\"alice\",\"number\":\"123\"}");

        assertThat(service.consumeForUser("alice")).isEmpty();
    }

    private GateNotificationService service() {
        return new GateNotificationService(mock(ConfigService.class));
    }
}
