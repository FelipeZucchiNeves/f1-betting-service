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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static java.time.LocalDateTime.now;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventSyncService {

    private final F1DataProvider f1DataProvider;
    private final EventRepository eventRepository;
    private final DriverRepository driverRepository;
    private final EventMapper eventMapper;
    private final DriverMapper driverMapper;
    private final CacheManager cacheManager;

    @Scheduled(initialDelayString = "${sync.initialDelay:1000}", fixedRateString = "${sync.fixedRate:30000}")
    public void scheduledSync() {
        log.info("Start sync events");
        boolean isDatabaseEmpty = eventRepository.count() == 0;
        if (isDatabaseEmpty) {
            log.info("Database is empty. Triggering INITIAL FULL SYNC...");
            startSync(true);
        } else {
            log.info("Database already has data. Triggering PARTIAL SYNC...");
            startSync(false);
        }
    }

    public void startSync(boolean fullSync) {
        CompletableFuture.runAsync(() -> {
            log.info("Starting full synchronization (bulk mode)...");
            List<DriverExternalDTO> drivers = f1DataProvider.findDrivers(null);
            List<SessionExternalDTO> sessions = f1DataProvider.findSessions(null, null, null);

            if (sessions == null || sessions.isEmpty()) {
                return;
            }

            Map<Integer, List<DriverExternalDTO>> driversBySession = drivers == null
                    ? Collections.emptyMap()
                    : drivers.stream()
                    .filter(d -> d.getSessionKey() != null)
                    .collect(Collectors.groupingBy(DriverExternalDTO::getSessionKey));

            for (SessionExternalDTO session : sessions) {
                Integer sessionKey = session.getSessionKey();

                if (!fullSync && session.getDateStart() != null) {
                    LocalDateTime startTime = OffsetDateTime.parse(session.getDateStart()).toLocalDateTime();
                    if (startTime.isBefore(now())) {
                        continue;
                    }
                }

                Event event = eventRepository.findByExternalSessionKey(sessionKey)
                        .orElseGet(() -> eventRepository.save(eventMapper.mapToEvent(session)));

                if (event.getDrivers() == null) {
                    event.setDrivers(new HashSet<>());
                }

                List<DriverExternalDTO> driversForSession = driversBySession.getOrDefault(sessionKey, Collections.emptyList());

                if (driversForSession == null || driversForSession.isEmpty()) {
                    eventRepository.save(event);
                    continue;
                }

                List<Integer> externalIds = driversForSession.stream()
                        .map(DriverExternalDTO::getDriverNumber)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(Collectors.toList());

                List<Driver> existing = externalIds.isEmpty()
                        ? Collections.emptyList()
                        : driverRepository.findAllByExternalDriverIdIn(externalIds);

                Set<Integer> existingIds = existing.stream()
                        .map(Driver::getExternalDriverId)
                        .collect(Collectors.toSet());

                List<Driver> toCreate = new ArrayList<>();
                for (DriverExternalDTO dDto : driversForSession) {
                    Integer extId = dDto.getDriverNumber();
                    if (extId == null) continue;
                    if (!existingIds.contains(extId)) {
                        toCreate.add(driverMapper.mapToDriverMarket(dDto));
                    }
                }

                List<Driver> created = toCreate.isEmpty() ? Collections.emptyList() : driverRepository.saveAll(toCreate);

                event.getDrivers().addAll(existing);
                event.getDrivers().addAll(created);

                eventRepository.save(event);
            }

            Cache cache = cacheManager.getCache("events_list");
            if (cache != null) {
                cache.clear();
                log.info("Cache 'events_list' cleared after sync.");
            }
        });
    }
}