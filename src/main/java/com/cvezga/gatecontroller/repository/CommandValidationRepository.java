package com.cvezga.gatecontroller.repository;

import com.cvezga.gatecontroller.entity.Event;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Provides persistence and reverse-chronological retrieval of audit events.
 */
public interface EventRepository extends CrudRepository<Event, Long> {

    /**
     * Retrieves all events with the most recent event first.
     *
     * @return events ordered by timestamp in descending order
     */
    List<Event> findAllByOrderByDateTimeDesc();
}
