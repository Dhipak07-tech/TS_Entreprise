package com.connectit.core.sla.entity;

import com.connectit.core.ticket.entity.Ticket;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "TICKET_SLA_TRACKING")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketSlaTracking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TICKET_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_SLA_TRACKING_TICKETS"))
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SLA_POLICY_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_SLA_TRACKING_SLA_POLICIES"))
    private SlaPolicy slaPolicy;

    @Column(name = "RESPONSE_DEADLINE", nullable = false)
    private LocalDateTime responseDeadline;

    @Column(name = "RESOLUTION_DEADLINE", nullable = false)
    private LocalDateTime resolutionDeadline;

    @Column(name = "RESPONDED_AT")
    private LocalDateTime respondedAt;

    @Column(name = "RESOLVED_AT")
    private LocalDateTime resolvedAt;

    @Column(name = "IS_RESPONSE_BREACHED", nullable = false)
    private Boolean isResponseBreached = false;

    @Column(name = "IS_RESOLUTION_BREACHED", nullable = false)
    private Boolean isResolutionBreached = false;
}
