package com.cvezga.gatecontroller.controller;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ExtendedModelMap;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link UserPageController}.
 */
class UserPageControllerTest {

    @Test
    void usersReturnsViewWithPageMetadata() {
        ExtendedModelMap model = new ExtendedModelMap();

        assertThat(new UserPageController().users(model)).isEqualTo("users");
        assertThat(model).containsEntry("pageTitle", "User Management")
                .containsEntry("applicationName", "Gate Controller");
    }
}
