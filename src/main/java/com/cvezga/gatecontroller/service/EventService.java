package com.cvezga.gatecontroller.service;

import com.cvezga.gatecontroller.entity.Event;
import com.cvezga.gatecontroller.repository.EventRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

/**
 * Records user activity and retrieves the audit history in reverse
 * chronological order.
 */
@Service
public class EventService {

    private final EventRepository eventRepository;
    private final ZoneId timezone;

    public EventService(
            EventRepository eventRepository,
            @Value("${timezone:UTC}") String timezone) {
        this.eventRepository = eventRepository;
        this.timezone = ZoneId.of(timezone);
    }

    public void saveEvent(String username, String type, String message) {
        Event event = new Event();
        event.setDateTime(LocalDateTime.now(timezone));
        event.setUsername(username);
        event.setType(type);
        event.setMessage(message);
        eventRepository.save(event);
    }

    public List<Event> findAllNewestFirst() {
        return eventRepository.findAllByOrderByDateTimeDesc();
    }
}
