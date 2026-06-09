package com.connectit.core.sla.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "SLA_POLICIES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SlaPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    @Column(name = "PRIORITY", nullable = false, unique = true, length = 50)
    private String priority; // CRITICAL, HIGH, MEDIUM, LOW

    @Column(name = "RESPONSE_TIME_MINS", nullable = false)
    private Integer responseTimeMins;

    @Column(name = "RESOLUTION_TIME_MINS", nullable = false)
    private Integer resolutionTimeMins;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive = true;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
