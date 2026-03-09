package com.f1betting.integration;

import com.f1betting.dto.external.DriverExternalDTO;
import com.f1betting.dto.external.SessionExternalDTO;
import com.f1betting.entity.Driver;
import com.f1betting.entity.Event;
import com.f1betting.mapper.DriverMapper;
import com.f1betting.mapper.EventMapper;
import com.f1betting.provider.F1DataProvider;
import com.f1betting.repository.DriverRepository;
import com.f1betting.repository.EventRepository;
import com.f1betting.service.EventSyncService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.when;

@SpringBootTest
class EventSyncServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private EventSyncService eventSyncService;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private DriverRepository driverRepository;

    @MockBean
    private F1DataProvider f1DataProvider;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private EventMapper eventMapper;

    @Autowired
    private DriverMapper driverMapper;

    private SessionExternalDTO session1;
    private DriverExternalDTO driverDto1;

    @BeforeEach
    void setUp() {
        driverRepository.deleteAll();
        eventRepository.deleteAll();

        session1 = new SessionExternalDTO();
        session1.setSessionKey(1);
        session1.setSessionName("Grand Prix Test");
        session1.setDateStart(OffsetDateTime.now().plusDays(1).toString());
        session1.setYear(OffsetDateTime.parse(session1.getDateStart()).getYear());
        session1.setCountryName("United Kingdom");

        driverDto1 = new DriverExternalDTO();
        driverDto1.setDriverNumber(101);
        driverDto1.setFullName("Test Driver");
        driverDto1.setSessionKey(1);
    }

    @Test
    @DisplayName("Full sync should create event and driver and link them")
    void fullSync_createsEventAndDriver() {
        when(f1DataProvider.findSessions(null, null, null)).thenReturn(List.of(session1));
        when(f1DataProvider.findDrivers(null)).thenReturn(List.of(driverDto1));

        eventSyncService.startSync(true);

        await().atMost(5, TimeUnit.SECONDS).untilAsserted(() -> {
            transactionTemplate.execute(status -> {
                List<Event> events = eventRepository.findAll();
                assertThat(events).hasSize(1);

                Event event = events.get(0);
                assertThat(event.getExternalSessionKey()).isEqualTo(1);

                assertThat(event.getDrivers()).isNotEmpty();

                Driver driver = event.getDrivers().iterator().next();
                assertThat(driver.getExternalDriverId()).isEqualTo(101);
                return null;
            });
        });
    }


    @Test
    @DisplayName("Partial sync should skip past sessions")
    @Transactional
    void partialSync_skipsPastSessions() throws InterruptedException {
        SessionExternalDTO pastSession = new SessionExternalDTO();
        pastSession.setSessionKey(2);
        pastSession.setSessionName("Past GP");
        pastSession.setDateStart(OffsetDateTime.now().minusDays(1).toString());

        when(f1DataProvider.findSessions(null, null, null)).thenReturn(List.of(session1, pastSession));
        when(f1DataProvider.findDrivers(null)).thenReturn(List.of(driverDto1));

        eventSyncService.startSync(false);

        Thread.sleep(1000);

        List<Event> events = eventRepository.findAll();
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getExternalSessionKey()).isEqualTo(session1.getSessionKey());
    }

    @Test
    @DisplayName("Sync with no sessions should not create any event")
    @Transactional
    void syncWithNoSessions_createsNothing() throws InterruptedException {
        when(f1DataProvider.findSessions(null, null, null)).thenReturn(List.of());
        when(f1DataProvider.findDrivers(null)).thenReturn(List.of());

        eventSyncService.startSync(true);

        Thread.sleep(500);

        assertThat(eventRepository.findAll()).isEmpty();
        assertThat(driverRepository.findAll()).isEmpty();
    }
}