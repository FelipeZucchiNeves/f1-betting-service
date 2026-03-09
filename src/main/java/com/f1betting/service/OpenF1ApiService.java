package com.f1betting.service;

import com.f1betting.client.OpenF1Client;
import com.f1betting.dto.external.DriverExternalDTO;
import com.f1betting.dto.external.SessionExternalDTO;
import com.f1betting.provider.F1DataProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service wrapper for OpenF1 API calls with caching.
 * Caching is used to respect external API rate limits and improve performance.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OpenF1ApiService implements F1DataProvider {

    private final OpenF1Client openF1Client;

    /**
     * Fetch sessions from OpenF1 API with caching.
     * The cache key is composed of year, country, and sessionName to ensure unique results.
     */
    @Override
    public List<SessionExternalDTO> findSessions(Integer year, String country, String sessionName) {
        log.info("Fetching sessions from OpenF1 API - year: {}, country: {}, sessionName: {}",
                year, country, sessionName);

        return openF1Client.findSessions(year, country, sessionName);
    }

    /**
     * Fetch drivers for a session from OpenF1 API with caching.
     * Drivers are cached by sessionKey as they rarely change for a specific session.
     */
    @Override
    @Cacheable(value = "drivers", key = "#sessionKey != null ? #sessionKey : 'all'")
    public List<DriverExternalDTO> findDrivers(Integer sessionKey) {
        log.info("Fetching drivers from OpenF1 API for session: {}", sessionKey);

        return openF1Client.findDrivers(sessionKey);
    }
}