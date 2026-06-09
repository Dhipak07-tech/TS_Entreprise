package com.connectit.core.notification.entity;

import com.connectit.core.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICATIONS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RECIPIENT_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_NOTIFICATIONS_USERS"))
    private User recipient;

    @Column(name = "TITLE", nullable = false, length = 255)
    private String title;

    @Column(name = "MESSAGE", nullable = false, length = 2000)
    private String message;

    @Column(name = "IS_READ", nullable = false)
    private Boolean isRead = false;

    @Column(name = "TYPE", nullable = false, length = 50)
    private String type; // e.g. TICKET_ASSIGNED, SLA_WARNING, SYSTEM_ALERT

    @Column(name = "SENT_AT", nullable = false, updatable = false)
    private LocalDateTime sentAt = LocalDateTime.now();
}
