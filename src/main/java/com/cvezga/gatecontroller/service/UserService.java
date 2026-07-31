package com.cvezga.gatecontroller.service;

import com.cvezga.gatecontroller.entity.User;
import com.cvezga.gatecontroller.exception.UserNotFoundException;
import com.cvezga.gatecontroller.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implements user CRUD operations and protects stored credentials by encoding
 * new or changed passwords before persistence.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder  passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Iterable<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    public User create(User user) {
        user.setId(null);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User update(Long id, User updatedUser) {
        User existingUser = findById(id);

        existingUser.setUsername(updatedUser.getUsername());
        existingUser.setEmail(updatedUser.getEmail());
        existingUser.setFirstName(updatedUser.getFirstName());
        existingUser.setLastName(updatedUser.getLastName());
        existingUser.setRole(updatedUser.getRole());

        if (updatedUser.getPassword() != null
                && !updatedUser.getPassword().isBlank()) {
            existingUser.setPassword(
                    passwordEncoder.encode(updatedUser.getPassword())
            );
        }

        return userRepository.save(existingUser);
    }

    public void delete(Long id) {
        User user = findById(id);
        userRepository.delete(user);
    }
}
