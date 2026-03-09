package com.f1betting.service;

import com.f1betting.dto.response.DriverMarketResponse;
import com.f1betting.dto.response.EventResponse;
import com.f1betting.entity.Event;
import com.f1betting.exception.ResourceNotFoundException;
import com.f1betting.mapper.DriverMapper;
import com.f1betting.mapper.EventMapper;
import com.f1betting.repository.DriverRepository;
import com.f1betting.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final DriverRepository driverRepository;
    private final EventMapper eventMapper;
    private final DriverMapper driverMapper;


    /**
     * List events with pagination: synchronize (attempt) and use DB pageable.
     * If synchronization fails, serve DB page and DO NOT call external driver API.
     */
    @Transactional(readOnly = true)
    @Cacheable(value = "events_list", key = "#year + '-' + #country + '-' + #sessionType")
    public List<EventResponse> listEvents (Integer year, String country, String sessionType) {
        log.info("Listing events - year: {}, country: {}, sessionType: {}",
                year, country, sessionType);

        List<Event> eventPage = eventRepository.findByFiltersWithPagination(year, country, sessionType);

        return eventPage.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

    }

    private EventResponse mapToResponse(Event event) {
        List<DriverMarketResponse> driverMarket = event.getDrivers().stream()
                .map(driverMapper::mapToDriverMarketResponse)
                .collect(Collectors.toList());

        return eventMapper.mapToEventResponse(event, driverMarket);
    }

    @Transactional(readOnly = true)
    public Event findEventById(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event", eventId));
    }
}