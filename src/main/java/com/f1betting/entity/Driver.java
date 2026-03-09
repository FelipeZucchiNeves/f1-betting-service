package com.f1betting.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Driver entity representing an F1 driver.
 */
@Entity
@Table(name = "drivers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Driver {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "external_driver_id", nullable = false, unique = true)
    private Integer externalDriverId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(name = "driver_number")
    private Integer driverNumber;

    @Column(name = "team_name")
    private String teamName;

    @Column(name = "country_code")
    private String countryCode;

    @ManyToMany(mappedBy = "drivers")
    private Set<Event> events = new HashSet<>();;
}
