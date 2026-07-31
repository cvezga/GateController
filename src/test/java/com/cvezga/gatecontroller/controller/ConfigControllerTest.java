package com.cvezga.gatecontroller.controller;

import com.cvezga.gatecontroller.entity.Config;
import com.cvezga.gatecontroller.service.ConfigService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.web.servlet.mvc.support.RedirectAttributesModelMap;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for configuration page and mutation behavior in
 * {@link ConfigController}.
 */
class ConfigControllerTest {

    private ConfigService service;
    private ConfigController controller;

    @BeforeEach
    void setUp() {
        service = mock(ConfigService.class);
        controller = new ConfigController(service);
    }

    @Test
    void pageUsesExistingConfiguration() {
        Config config = new Config();
        when(service.find()).thenReturn(Optional.of(config));
        ExtendedModelMap model = new ExtendedModelMap();

        assertThat(controller.page(model)).isEqualTo("config");
        assertThat(model).containsEntry("pageTitle", "Configuration")
                .containsEntry("applicationName", "Gate Controller")
                .containsEntry("config", config)
                .containsEntry("configExists", true);
    }

    @Test
    void pageCreatesEmptyConfigurationWhenNoneExists() {
        when(service.find()).thenReturn(Optional.empty());
        ExtendedModelMap model = new ExtendedModelMap();

        controller.page(model);

        assertThat(model.get("config")).isInstanceOf(Config.class);
        assertThat(model).containsEntry("configExists", false);
    }

    @Test
    void saveDelegatesAndRedirectsWithMessage() {
        Config config = new Config();
        RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();

        assertThat(controller.save(config, attributes)).isEqualTo("redirect:/config");
        verify(service).save(config);
        assertThat(attributes.getFlashAttributes().get("message"))
                .isEqualTo("Configuration saved successfully");
    }

    @Test
    void deleteDelegatesAndRedirectsWithMessage() {
        RedirectAttributesModelMap attributes = new RedirectAttributesModelMap();

        assertThat(controller.delete(attributes)).isEqualTo("redirect:/config");
        verify(service).delete();
        assertThat(attributes.getFlashAttributes().get("message"))
                .isEqualTo("Configuration deleted successfully");
    }
}
