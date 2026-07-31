package com.cvezga.gatecontroller.repository;

import com.cvezga.gatecontroller.entity.Config;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Provides persistence operations for the singleton {@link Config} record.
 */
public interface ConfigRepository extends JpaRepository<Config, Long> {
}
