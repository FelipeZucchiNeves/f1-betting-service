package com.f1betting.mapper;

import com.f1betting.dto.external.DriverExternalDTO;
import com.f1betting.dto.response.DriverMarketResponse;
import com.f1betting.entity.Driver;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;

import static com.f1betting.util.OddGeneration.generateRandomOdds;

@Service
public class DriverMapper {

    public DriverMarketResponse mapToDriverMarketResponse (Driver driver) {
        return new DriverMarketResponse(
                driver.getId(),
                driver.getExternalDriverId(),
                driver.getFullName(),
                driver.getDriverNumber(),
                driver.getTeamName(),
                generateRandomOdds()
        );
    }

    public Driver mapToDriverMarket (DriverExternalDTO dto) {
        return Driver.builder()
                .externalDriverId(dto.getDriverNumber())
                .fullName(dto.getFullName())
                .driverNumber(dto.getDriverNumber())
                .teamName(dto.getTeamName())
                .countryCode(dto.getCountryCode())
                .build();
    }

    public List<Driver> mapToDriverMarket (List<DriverExternalDTO> drivers) {
        return drivers.stream().map(this::mapToDriverMarket).collect(Collectors.toList());
    }
}
