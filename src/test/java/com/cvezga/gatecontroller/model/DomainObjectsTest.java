package com.cvezga.gatecontroller.model;

import com.cvezga.gatecontroller.dto.UserResponse;
import com.cvezga.gatecontroller.entity.Event;
import com.cvezga.gatecontroller.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Verifies the value-storage contracts of forms, entities, and DTOs.
 */
class DomainObjectsTest {

    @Test
    void loginFormStoresValuesAndHasUsefulStringRepresentation() {
        LoginForm form = new LoginForm();
        form.setUser("alice");
        form.setPassword("secret");

        assertThat(form.getUser()).isEqualTo("alice");
        assertThat(form.getPassword()).isEqualTo("secret");
        assertThat(form.toString()).contains("alice", "secret");
    }

    @Test
    void buttonFormSupportsLombokGeneratedValueMethods() {
        ButtonForm first = new ButtonForm();
        ButtonForm second = new ButtonForm();
        first.setUser("alice");
        second.setUser("alice");

        assertThat(first.getUser()).isEqualTo("alice");
        assertThat(first).isEqualTo(second).hasSameHashCodeAs(second);
        assertThat(first.toString()).contains("alice");
    }

    @Test
    void userAndEventStoreTheirProperties() {
        User user = new User();
        user.setId(1L);
        user.setUsername("alice");
        user.setPassword("encoded");
        user.setEmail("alice@example.com");
        user.setFirstName("Alice");
        user.setLastName("Example");
        user.setRole("USER");

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUsername()).isEqualTo("alice");
        assertThat(user.getPassword()).isEqualTo("encoded");
        assertThat(user.getEmail()).isEqualTo("alice@example.com");
        assertThat(user.getFirstName()).isEqualTo("Alice");
        assertThat(user.getLastName()).isEqualTo("Example");
        assertThat(user.getRole()).isEqualTo("USER");

        LocalDateTime time = LocalDateTime.now();
        Event event = new Event();
        event.setId(2L);
        event.setDateTime(time);
        event.setUsername("alice");
        event.setType("button");
        event.setMessage("opened");

        assertThat(event.getId()).isEqualTo(2L);
        assertThat(event.getDateTime()).isEqualTo(time);
        assertThat(event.getUsername()).isEqualTo("alice");
        assertThat(event.getType()).isEqualTo("button");
        assertThat(event.getMessage()).isEqualTo("opened");
    }

    @Test
    void userResponseExposesRecordComponents() {
        UserResponse response = new UserResponse(1L, "alice", "a@b.test", "Alice", "A", "ADMIN");

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.email()).isEqualTo("a@b.test");
        assertThat(response.firstName()).isEqualTo("Alice");
        assertThat(response.lastName()).isEqualTo("A");
        assertThat(response.role()).isEqualTo("ADMIN");
    }
}
