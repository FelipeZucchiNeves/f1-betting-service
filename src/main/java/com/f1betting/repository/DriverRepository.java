package com.f1betting.repository;

import com.f1betting.entity.Driver;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Driver entity operations.
 */
@Repository
public interface DriverRepository extends JpaRepository<Driver, Long> {
    Optional<Driver> findByExternalDriverId(Integer externalDriverId);
    List<Driver> findAllByExternalDriverIdIn(List<Integer> externalIds);
}
