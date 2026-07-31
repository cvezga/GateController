package com.cvezga.gatecontroller.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Adds application metadata shared by all MVC views to their models.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    private final String applicationName;
    private final String applicationVersion;

    public GlobalModelAttributes(
            @Value("${spring.application.name}") String applicationName,
            @Value("${version}") String applicationVersion) {
        this.applicationName = applicationName;
        this.applicationVersion = applicationVersion;
    }

    @ModelAttribute("applicationName")
    public String applicationName() {
        return applicationName;
    }

    @ModelAttribute("applicationVersion")
    public String applicationVersion() {
        return applicationVersion;
    }
}
