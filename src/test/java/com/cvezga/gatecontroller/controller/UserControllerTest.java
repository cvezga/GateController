package com.cvezga.gatecontroller.controller;

import com.cvezga.gatecontroller.entity.User;
import com.cvezga.gatecontroller.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the REST response and service-delegation behavior of
 * {@link UserController}.
 */
class UserControllerTest {

    private UserService service;
    private UserController controller;

    @BeforeEach
    void setUp() {
        service = mock(UserService.class);
        controller = new UserController(service);
    }

    @Test
    void delegatesReadOperations() {
        User user = new User();
        when(service.findAll()).thenReturn(List.of(user));
        when(service.findById(7L)).thenReturn(user);

        assertThat(controller.findAll()).containsExactly(user);
        assertThat(controller.findById(7L)).isSameAs(user);
    }

    @Test
    void createReturnsCreatedResponseAndLocation() {
        User submitted = new User();
        User created = new User();
        created.setId(9L);
        when(service.create(submitted)).thenReturn(created);

        var response = controller.create(submitted);

        assertThat(response.getStatusCode().value()).isEqualTo(201);
        assertThat(response.getHeaders().getLocation()).hasToString("/api/users/9");
        assertThat(response.getBody()).isSameAs(created);
    }

    @Test
    void updateAndDeleteDelegateToService() {
        User submitted = new User();
        User updated = new User();
        when(service.update(4L, submitted)).thenReturn(updated);

        assertThat(controller.update(4L, submitted)).isSameAs(updated);
        controller.delete(4L);

        verify(service).delete(4L);
    }
}
