package com.f1betting.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Event entity representing an F1 race session.
 */
@Entity
@Table(name = "events", uniqueConstraints = {
    @UniqueConstraint(columnNames = "externalSessionKey")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_session_key", nullable = false, unique = true)
    private Integer externalSessionKey;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private String country;

    @Column(name = "session_name", nullable = false)
    private String sessionName;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "circuit_short_name")
    private String circuitShortName;

    @Column(name = "winner_driver_id")
    private Long winnerDriverId;

    @Column(name = "settled", nullable = false)
    @Builder.Default
    private Boolean settled = false;

    @ManyToMany
    @JoinTable(
            name = "event_drivers",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "driver_id"),
            uniqueConstraints = @UniqueConstraint(columnNames = {"event_id", "driver_id"})
    )
    private Set<Driver> drivers = new HashSet<>();
}
