package com.f1betting.provider;

import com.f1betting.dto.external.DriverExternalDTO;
import com.f1betting.dto.external.SessionExternalDTO;

import java.util.List;

public interface F1DataProvider {
    List<SessionExternalDTO> findSessions(Integer year, String country, String sessionName);
    List<DriverExternalDTO> findDrivers(Integer sessionKey);
}
