package com.cvezga.gatecontroller.repository;

import com.cvezga.gatecontroller.entity.User;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.Repository;

import java.util.Optional;

/**
 * Provides persistence and identity-based lookup operations for application
 * users.
 */
public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);
}
