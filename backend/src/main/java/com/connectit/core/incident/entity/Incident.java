package com.connectit.core.incident.entity;

import com.connectit.core.ticket.entity.Ticket;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "INCIDENTS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "IMPACT", nullable = false, length = 50)
    private String impact; // HIGH, MEDIUM, LOW

    @Column(name = "URGENCY", nullable = false, length = 50)
    private String urgency; // HIGH, MEDIUM, LOW

    @Column(name = "CATEGORY", nullable = false, length = 100)
    private String category;

    @Column(name = "SUBCATEGORY", length = 100)
    private String subcategory;

    @Column(name = "MAJOR_INCIDENT", nullable = false)
    private Boolean majorIncident = false;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "INCIDENT_TICKETS",
            joinColumns = @JoinColumn(name = "INCIDENT_ID", foreignKey = @ForeignKey(name = "FK_INCIDENT_TICKETS_INCIDENTS")),
            inverseJoinColumns = @JoinColumn(name = "TICKET_ID", foreignKey = @ForeignKey(name = "FK_INCIDENT_TICKETS_TICKETS"))
    )
    private Set<Ticket> tickets = new HashSet<>();
}
