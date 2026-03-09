package com.f1betting.service;

import com.f1betting.entity.Driver;
import com.f1betting.exception.ResourceNotFoundException;
import com.f1betting.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for driver-related operations.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    /**
     * Find driver by ID or throw exception.
     */
    @Transactional(readOnly = true)
    public Driver findDriverById(Long driverId) {
        return driverRepository.findById(driverId)
                .orElseThrow(() -> new ResourceNotFoundException("Driver", driverId));
    }
}
