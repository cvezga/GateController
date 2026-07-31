package com.cvezga.gatecontroller.service;

import com.cvezga.gatecontroller.entity.Event;
import com.cvezga.gatecontroller.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Records user activity and retrieves the audit history in reverse
 * chronological order.
 */
@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public void saveEvent(String username, String type, String message) {
        Event event = new Event();
        event.setDateTime(LocalDateTime.now());
        event.setUsername(username);
        event.setType(type);
        event.setMessage(message);
        eventRepository.save(event);
    }

    public List<Event> findAllNewestFirst() {
        return eventRepository.findAllByOrderByDateTimeDesc();
    }
}
