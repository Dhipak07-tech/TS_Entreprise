package com.connectit.core.change.entity;

import com.connectit.core.ticket.entity.Ticket;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "CHANGES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Change {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TITLE", nullable = false, length = 255)
    private String title;

    @Column(name = "DESCRIPTION", nullable = false, length = 4000)
    private String description;

    @Column(name = "CHANGE_TYPE", nullable = false, length = 50)
    private String changeType; // STANDARD, NORMAL, EMERGENCY

    @Column(name = "RISK_LEVEL", nullable = false, length = 50)
    private String riskLevel; // HIGH, MEDIUM, LOW

    @Column(name = "STATUS", nullable = false, length = 50)
    private String status; // DRAFT, CAB_APPROVAL, SCHEDULED, IMPLEMENTING, REVIEW, CLOSED

    @Column(name = "ROLLBACK_PLAN", nullable = false, length = 4000)
    private String rollbackPlan;

    @Column(name = "TEST_PLAN", nullable = false, length = 4000)
    private String testPlan;

    @Column(name = "PLANNED_START", nullable = false)
    private LocalDateTime plannedStart;

    @Column(name = "PLANNED_END", nullable = false)
    private LocalDateTime plannedEnd;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "CHANGE_TICKET_LINKS",
            joinColumns = @JoinColumn(name = "CHANGE_ID", foreignKey = @ForeignKey(name = "FK_CHANGE_LINKS_CHANGES")),
            inverseJoinColumns = @JoinColumn(name = "TICKET_ID", foreignKey = @ForeignKey(name = "FK_CHANGE_LINKS_TICKETS"))
    )
    private Set<Ticket> tickets = new HashSet<>();
}
