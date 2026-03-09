package com.f1betting.mapper;

import com.f1betting.dto.external.SessionExternalDTO;
import com.f1betting.dto.response.DriverMarketResponse;
import com.f1betting.dto.response.EventResponse;
import com.f1betting.entity.Event;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class EventMapper {

    public Event mapToEvent (SessionExternalDTO session) {

        LocalDateTime startTime = null;
        if (session.getDateStart() != null) {
            startTime = OffsetDateTime.parse(session.getDateStart()).toLocalDateTime();
        }
        return Event.builder()
                .externalSessionKey(session.getSessionKey())
                .year(session.getYear())
                .country(session.getCountryName())
                .sessionName(session.getSessionName())
                .startTime(startTime)
                .circuitShortName(session.getCircuitShortName())
                .build();
    }

    public EventResponse mapToEventResponse (Event event, List<DriverMarketResponse> driverMarket) {
        return new EventResponse(
                event.getId(),
                event.getExternalSessionKey(),
                event.getYear(),
                event.getCountry(),
                event.getSessionName(),
                event.getStartTime(),
                event.getCircuitShortName(),
                driverMarket,
                !driverMarket.isEmpty()
        );
    }
}
