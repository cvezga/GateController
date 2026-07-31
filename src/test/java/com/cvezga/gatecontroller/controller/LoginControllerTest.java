package com.cvezga.gatecontroller.controller;

import com.cvezga.gatecontroller.model.LoginForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link LoginController}.
 *
 * <p>The controller is invoked directly with an {@link ExtendedModelMap}, so these
 * tests verify its view selection and model-building behavior without starting a
 * Spring application context.</p>
 */
class LoginControllerTest {

    private LoginController controller;
    private ExtendedModelMap model;

    /**
     * Creates a fresh controller and model for every test to prevent model
     * attributes from one scenario affecting another.
     */
    @BeforeEach
    void setUp() {
        controller = new LoginController();
        model = new ExtendedModelMap();
    }

    /**
     * Verifies that a normal visit returns the login view with all attributes
     * required to render an empty login form and no status message.
     */
    @Test
    void homeReturnsLoginViewWithDefaultModel() {
        String viewName = controller.home(null, null, model);

        assertThat(viewName).isEqualTo("login");
        assertThat(model)
                .containsEntry("pageTitle", "Gate Controller")
                .containsEntry("applicationName", "Gate Controller")
                .containsEntry("username", "")
                .doesNotContainKey("message");
        assertThat(model.get("loginForm")).isInstanceOf(LoginForm.class);
    }

    /**
     * Verifies that the presence of the {@code error} request parameter produces
     * the authentication-failure message.
     */
    @Test
    void homeAddsErrorMessageWhenLoginFailed() {
        controller.home("true", null, model);

        assertThat(model.get("message")).isEqualTo("Invalid username or password.");
    }

    /**
     * Verifies that the presence of the {@code logout} request parameter informs
     * the user that their session ended successfully.
     */
    @Test
    void homeAddsLogoutMessageWhenUserSignedOut() {
        controller.home(null, "true", model);

        assertThat(model.get("message")).isEqualTo("You have been signed out.");
    }

    /**
     * Documents the controller's precedence rule: an authentication error is
     * displayed when both {@code error} and {@code logout} are present.
     */
    @Test
    void homeGivesErrorMessagePrecedenceWhenErrorAndLogoutArePresent() {
        controller.home("true", "true", model);

        assertThat(model.get("message")).isEqualTo("Invalid username or password.");
    }
}
