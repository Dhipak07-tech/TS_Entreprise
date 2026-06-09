package com.connectit.core.grc.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SECURITY_INCIDENTS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecurityIncident {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "INCIDENT_NUMBER", nullable = false, unique = true, length = 50)
    private String incidentNumber;

    @Column(name = "TITLE", nullable = false, length = 255)
    private String title;

    @Column(name = "DESCRIPTION", nullable = false, length = 4000)
    private String description;

    @Column(name = "SEVERITY", nullable = false, length = 50)
    private String severity; // CRITICAL, HIGH, MEDIUM, LOW

    @Column(name = "STATUS", nullable = false, length = 50)
    private String status; // OPEN, INVESTIGATING, RESOLVED, CLOSED

    @Column(name = "IDENTIFIED_AT", nullable = false)
    @Builder.Default
    private LocalDateTime identifiedAt = LocalDateTime.now();
}
