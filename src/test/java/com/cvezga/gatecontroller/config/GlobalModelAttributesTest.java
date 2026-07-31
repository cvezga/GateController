package com.cvezga.gatecontroller.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link GlobalModelAttributes}.
 */
class GlobalModelAttributesTest {

    @Test
    void exposesConfiguredApplicationMetadata() {
        GlobalModelAttributes attributes = new GlobalModelAttributes("Gate Controller", "1.2.3");

        assertThat(attributes.applicationName()).isEqualTo("Gate Controller");
        assertThat(attributes.applicationVersion()).isEqualTo("1.2.3");
    }
}
