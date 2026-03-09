package com.f1betting.client;

import com.f1betting.dto.external.DriverExternalDTO;
import com.f1betting.dto.external.SessionExternalDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/**
 * Feign client for OpenF1 API integration.
 * API documentation: https://openf1.org/
 */
@FeignClient(name = "openf1-client", url = "${openf1.api.base-url}")
public interface OpenF1Client {

    /**
     * Fetch F1 sessions (races, qualifying, practice, etc.)
     */
    @GetMapping("/sessions")
    List<SessionExternalDTO> findSessions(
            @RequestParam(value = "year", required = false) Integer year,
            @RequestParam(value = "country_name", required = false) String countryName,
            @RequestParam(value = "session_name", required = false) String sessionName
    );

    /**
     * Fetch drivers for a specific session.
     */
    @GetMapping("/drivers")
    List<DriverExternalDTO> findDrivers(
            @RequestParam("session_key") Integer sessionKey
    );
}
