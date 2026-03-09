package com.f1betting.service;

import com.f1betting.dto.response.EventResponse;
import com.f1betting.entity.Driver;
import com.f1betting.entity.Event;
import com.f1betting.exception.ResourceNotFoundException;
import com.f1betting.mapper.DriverMapper;
import com.f1betting.mapper.EventMapper;
import com.f1betting.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventService Unit Tests")
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private DriverMapper driverMapper;

    @InjectMocks
    private EventService eventService;

    private Driver testDriver;
    private Event event1;
    private Event event2;
    private Event event3;
    private EventResponse resp1;
    private EventResponse resp2;
    private EventResponse resp3;

    @BeforeEach
    void setUp() {
        testDriver = Driver.builder()
                .id(1L)
                .externalDriverId(1)
                .driverNumber(1)
                .fullName("Max Verstappen")
                .teamName("Red Bull Racing")
                .countryCode("NED")
                .build();

        event1 = createEvent(1L, 1001, "Monaco");
        event2 = createEvent(2L, 1002, "Spain");
        event3 = createEvent(3L, 1003, "Italy");

        event1.setDrivers(new HashSet<>(List.of(testDriver)));
        event2.setDrivers(new HashSet<>(List.of(testDriver)));
        event3.setDrivers(new HashSet<>(List.of(testDriver)));

        resp1 = mock(EventResponse.class);
        resp2 = mock(EventResponse.class);
        resp3 = mock(EventResponse.class);


        Map<Event, EventResponse> responseMap = new IdentityHashMap<>();
        responseMap.put(event1, resp1);
        responseMap.put(event2, resp2);
        responseMap.put(event3, resp3);
    }

    private Event createEvent(Long id, int sessionKey, String country) {
        return Event.builder()
                .id(id)
                .externalSessionKey(sessionKey)
                .year(2024)
                .country(country)
                .sessionName("Race")
                .startTime(LocalDateTime.of(2024, 5, 26, 15, 0))
                .circuitShortName(country.substring(0, Math.min(country.length(), 3)).toUpperCase())
                .build();
    }

    @Test
    @DisplayName("Should return mapped events list when repository has events")
    void listEvents_returnsMappedResponses() {
        List<Event> events = List.of(event1, event2, event3);

        when(eventRepository.findByFiltersWithPagination(any(), any(), any())).thenReturn(events);

        List<EventResponse> result = eventService.listEvents(null, null, null);

        assertThat(result).hasSize(3);

        verify(eventRepository).findByFiltersWithPagination(null, null, null);
        verify(eventMapper, times(3)).mapToEventResponse(any(Event.class), anyList());
        verify(driverMapper, times(3)).mapToDriverMarketResponse(any(Driver.class));
    }

    @Test
    @DisplayName("Should return empty list when repository returns no events")
    void listEvents_returnsEmptyList() {
        when(eventRepository.findByFiltersWithPagination(any(), any(), any())).thenReturn(Collections.emptyList());

        List<EventResponse> result = eventService.listEvents(null, null, null);

        assertThat(result).isEmpty();

        verify(eventRepository).findByFiltersWithPagination(null, null, null);
        verifyNoInteractions(eventMapper);
    }

    @Test
    @DisplayName("findEventById should return event when present")
    void findEventById_found() {
        when(eventRepository.findById(1L)).thenReturn(Optional.of(event1));

        Event result = eventService.findEventById(1L);

        assertThat(result).isSameAs(event1);
        verify(eventRepository).findById(1L);
    }

    @Test
    @DisplayName("findEventById should throw ResourceNotFoundException when missing")
    void findEventById_notFound() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> eventService.findEventById(99L));
        verify(eventRepository).findById(99L);
    }
}