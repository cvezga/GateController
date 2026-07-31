package com.cvezga.gatecontroller.service;

import com.cvezga.gatecontroller.entity.User;
import com.cvezga.gatecontroller.exception.UserNotFoundException;
import com.cvezga.gatecontroller.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for user lookup, mutation, deletion, and password handling in
 * {@link UserService}.
 */
class UserServiceTest {

    private UserRepository repository;
    private PasswordEncoder encoder;
    private UserService service;

    @BeforeEach
    void setUp() {
        repository = mock(UserRepository.class);
        encoder = mock(PasswordEncoder.class);
        service = new UserService(repository, encoder);
    }

    @Test
    void findAllAndFindByIdDelegateToRepository() {
        User user = new User();
        when(repository.findAll()).thenReturn(List.of(user));
        when(repository.findById(2L)).thenReturn(Optional.of(user));

        assertThat(service.findAll()).containsExactly(user);
        assertThat(service.findById(2L)).isSameAs(user);
    }

    @Test
    void findByIdThrowsDomainExceptionWhenMissing() {
        when(repository.findById(8L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(8L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessage("User not found with ID: 8");
    }

    @Test
    void createClearsIdAndEncodesPassword() {
        User user = new User();
        user.setId(99L);
        user.setPassword("plain");
        when(encoder.encode("plain")).thenReturn("encoded");
        when(repository.save(user)).thenReturn(user);

        assertThat(service.create(user)).isSameAs(user);
        assertThat(user.getId()).isNull();
        assertThat(user.getPassword()).isEqualTo("encoded");
    }

    @Test
    void updateCopiesFieldsAndEncodesNonBlankPassword() {
        User existing = user("old", "old@example.com", "encoded-old");
        User submitted = user("new", "new@example.com", "new-password");
        submitted.setFirstName("First");
        submitted.setLastName("Last");
        submitted.setRole("ADMIN");
        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(encoder.encode("new-password")).thenReturn("encoded-new");
        when(repository.save(existing)).thenReturn(existing);

        assertThat(service.update(5L, submitted)).isSameAs(existing);
        assertThat(existing.getUsername()).isEqualTo("new");
        assertThat(existing.getEmail()).isEqualTo("new@example.com");
        assertThat(existing.getFirstName()).isEqualTo("First");
        assertThat(existing.getLastName()).isEqualTo("Last");
        assertThat(existing.getRole()).isEqualTo("ADMIN");
        assertThat(existing.getPassword()).isEqualTo("encoded-new");
    }

    @Test
    void updateKeepsPasswordWhenSubmittedPasswordIsBlank() {
        User existing = user("old", "old@example.com", "encoded-old");
        User submitted = user("new", "new@example.com", " ");
        when(repository.findById(5L)).thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        service.update(5L, submitted);

        assertThat(existing.getPassword()).isEqualTo("encoded-old");
        verifyNoInteractions(encoder);
    }

    @Test
    void deleteFindsThenDeletesUser() {
        User user = new User();
        when(repository.findById(6L)).thenReturn(Optional.of(user));

        service.delete(6L);

        verify(repository).delete(user);
    }

    private User user(String username, String email, String password) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(password);
        return user;
    }
}
