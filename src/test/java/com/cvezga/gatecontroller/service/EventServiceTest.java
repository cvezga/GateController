package com.cvezga.gatecontroller.service;

import com.cvezga.gatecontroller.entity.Event;
import com.cvezga.gatecontroller.repository.EventRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

/**
 * Unit tests for event creation and ordered retrieval in {@link EventService}.
 */
class EventServiceTest {

    @Test
    void saveEventBuildsAndPersistsEvent() {
        EventRepository repository = mock(EventRepository.class);
        EventService service = new EventService(repository, "GMT-6");
        LocalDateTime before = LocalDateTime.now(java.time.ZoneId.of("GMT-6"));

        service.saveEvent("alice", "button", "opened gate");

        ArgumentCaptor<Event> captor = ArgumentCaptor.forClass(Event.class);
        verify(repository).save(captor.capture());
        Event event = captor.getValue();
        assertThat(event.getDateTime())
                .isBetween(before, LocalDateTime.now(java.time.ZoneId.of("GMT-6")));
        assertThat(event.getUsername()).isEqualTo("alice");
        assertThat(event.getType()).isEqualTo("button");
        assertThat(event.getMessage()).isEqualTo("opened gate");
    }

    @Test
    void findAllNewestFirstDelegatesToOrderedQuery() {
        EventRepository repository = mock(EventRepository.class);
        List<Event> events = List.of(new Event());
        when(repository.findAllByOrderByDateTimeDesc()).thenReturn(events);

        assertThat(new EventService(repository, "GMT-6").findAllNewestFirst()).isSameAs(events);
    }

    @Test
    void rejectsInvalidConfiguredTimezone() {
        assertThatThrownBy(() -> new EventService(mock(EventRepository.class), "not-a-timezone"))
                .isInstanceOf(java.time.DateTimeException.class);
    }
}
