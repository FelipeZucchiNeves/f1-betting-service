package com.f1betting.service;

import com.f1betting.dto.external.DriverExternalDTO;
import com.f1betting.dto.external.SessionExternalDTO;
import com.f1betting.entity.Driver;
import com.f1betting.entity.Event;
import com.f1betting.mapper.DriverMapper;
import com.f1betting.mapper.EventMapper;
import com.f1betting.provider.F1DataProvider;
import com.f1betting.repository.DriverRepository;
import com.f1betting.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventSyncService - Synchronization tests")
class EventSyncServiceTest {

    @Mock
    private F1DataProvider f1DataProvider;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private EventMapper eventMapper;

    @Mock
    private DriverMapper driverMapper;

    @InjectMocks
    private EventSyncService eventSyncService;

    private SessionExternalDTO session1;
    private DriverExternalDTO driverDto1;
    private Event event1;
    private Driver driver1;

    @BeforeEach
    void setup() {
        session1 = new SessionExternalDTO();
        session1.setSessionKey(1);
        session1.setDateStart(OffsetDateTime.now().plusDays(1).toString());

        driverDto1 = new DriverExternalDTO();
        driverDto1.setDriverNumber(101);
        driverDto1.setSessionKey(1);

        event1 = new Event();
        event1.setExternalSessionKey(1);
        event1.setDrivers(new HashSet<>());

        driver1 = new Driver();
        driver1.setExternalDriverId(101);
    }

    @Test
    @DisplayName("Should skip expired sessions during partial synchronization")
    void startSync_partialSync_skipsExpired() {
        SessionExternalDTO expiredSession = new SessionExternalDTO();
        expiredSession.setSessionKey(2);
        expiredSession.setDateStart(OffsetDateTime.now().minusDays(1).toString());

        eventSyncService.startSync(false);

        verify(eventRepository, never()).findByExternalSessionKey(2);
    }

    @Test
    @DisplayName("Should associate existing drivers without creating duplicates")
    void startSync_usesExistingDrivers() {
        when(f1DataProvider.findSessions(null, null, null)).thenReturn(List.of(session1));
        when(f1DataProvider.findDrivers(null)).thenReturn(List.of(driverDto1));
        when(eventRepository.findByExternalSessionKey(1)).thenReturn(Optional.of(event1));
        when(driverRepository.findAllByExternalDriverIdIn(anyList())).thenReturn(List.of(driver1));

        eventSyncService.startSync(true);

        verify(driverRepository, never()).saveAll(anyList());

        assertThat(event1.getDrivers())
                .extracting(Driver::getExternalDriverId)
                .contains(driver1.getExternalDriverId());
    }
    @Test
    @DisplayName("Should abort sync when API returns no sessions")
    void startSync_noSessions_aborts() {
        eventSyncService.startSync(true);

        verify(eventRepository, never()).findByExternalSessionKey(anyInt());
        verify(driverRepository, never()).findAllByExternalDriverIdIn(anyList());
    }
}