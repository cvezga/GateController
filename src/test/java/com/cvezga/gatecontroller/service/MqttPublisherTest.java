package com.cvezga.gatecontroller.service;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for non-network failure paths in {@link MqttPublisher}.
 */
class MqttPublisherTest {

    @Test
    void publishReturnsFalseWithoutConfiguration() {
        ConfigService configService = mock(ConfigService.class);
        when(configService.find()).thenReturn(Optional.empty());

        assertThat(new MqttPublisher(configService).publishOpenCommand()).isFalse();
        verify(configService).find();
    }

    @Test
    void publishReturnsFalseWhenConfigurationLookupFails() {
        ConfigService configService = mock(ConfigService.class);
        when(configService.find()).thenThrow(new IllegalStateException("database unavailable"));

        assertThat(new MqttPublisher(configService).publishOpenCommand()).isFalse();
    }
}
