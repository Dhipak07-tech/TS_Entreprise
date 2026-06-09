package com.connectit.core.ticket.entity;

import com.connectit.core.team.entity.Team;
import com.connectit.core.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "TICKETS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TICKET_NUMBER", nullable = false, unique = true, length = 50)
    private String ticketNumber;

    @Column(name = "TITLE", nullable = false, length = 255)
    private String title;

    @Column(name = "DESCRIPTION", nullable = false, length = 4000)
    private String description;

    @Column(name = "STATUS", nullable = false, length = 50)
    private String status; // NEW, OPEN, IN_PROGRESS, ON_HOLD, RESOLVED, CLOSED, CANCELLED

    @Column(name = "PRIORITY", nullable = false, length = 50)
    private String priority; // CRITICAL, HIGH, MEDIUM, LOW

    @Column(name = "SOURCE", nullable = false, length = 50)
    private String source; // WEB, EMAIL, CHAT, API

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "REQUESTER_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_TICKETS_REQUESTER"))
    private User requester;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ASSIGNED_TEAM_ID", foreignKey = @ForeignKey(name = "FK_TICKETS_TEAMS"))
    private Team assignedTeam;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ASSIGNED_USER_ID", foreignKey = @ForeignKey(name = "FK_TICKETS_USERS"))
    private User assignedUser;

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "TICKET_WATCHERS",
            joinColumns = @JoinColumn(name = "TICKET_ID", foreignKey = @ForeignKey(name = "FK_WATCHERS_TICKETS")),
            inverseJoinColumns = @JoinColumn(name = "USER_ID", foreignKey = @ForeignKey(name = "FK_WATCHERS_USERS"))
    )
    private Set<User> watchers = new HashSet<>();

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
}
