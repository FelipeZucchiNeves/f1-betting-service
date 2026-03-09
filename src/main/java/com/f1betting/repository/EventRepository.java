package com.f1betting.repository;

import com.f1betting.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Event entity operations.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    
    Optional<Event> findByExternalSessionKey(Integer externalSessionKey);
    
    @Query("SELECT e FROM Event e WHERE " +
           "(:year IS NULL OR e.year = :year) AND " +
           "(:country IS NULL OR LOWER(e.country) LIKE LOWER(CONCAT('%', :country, '%'))) AND " +
           "(:sessionName IS NULL OR LOWER(e.sessionName) LIKE LOWER(CONCAT('%', :sessionName, '%')))")
    List<Event> findByFilters(@Param("year") Integer year, 
                              @Param("country") String country, 
                              @Param("sessionName") String sessionName);
    
    /**
     * Find events with pagination and optional filters.
     * Supports sorting by any field (e.g., startTime, country, year).
     */
    @Query("SELECT e FROM Event e WHERE " +
            "(:year IS NULL OR e.year = :year) AND " +
            "(:country IS NULL OR e.country = :country) AND " +
            "(:sessionName IS NULL OR e.sessionName = :sessionName)")
    List<Event> findByFiltersWithPagination(@Param("year") Integer year,
                                            @Param("country") String country,
                                            @Param("sessionName") String sessionName);
}
