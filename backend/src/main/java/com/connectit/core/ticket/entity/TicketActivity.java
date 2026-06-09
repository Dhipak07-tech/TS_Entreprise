package com.connectit.core.ticket.entity;

import com.connectit.core.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "TICKET_ACTIVITIES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TicketActivity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TICKET_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_ACTIVITIES_TICKETS"))
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ACTOR_ID", foreignKey = @ForeignKey(name = "FK_ACTIVITIES_USERS"))
    private User actor;

    @Column(name = "ACTIVITY_TYPE", nullable = false, length = 80)
    private String activityType; // STATUS_CHANGE, ASSIGNED, COMMENT_ADDED, SLA_BREACHED

    @Column(name = "OLD_VALUE", length = 255)
    private String oldValue;

    @Column(name = "NEW_VALUE", length = 255)
    private String newValue;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @CreatedDate
    @Column(name = "OCCURRED_AT", nullable = false, updatable = false)
    private LocalDateTime occurredAt;
}
