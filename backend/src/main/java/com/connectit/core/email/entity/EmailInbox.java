package com.connectit.core.email.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "EMAIL_INBOXES")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailInbox {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "EMAIL_ADDRESS", nullable = false, unique = true, length = 255)
    private String emailAddress;

    @Column(name = "IMAP_HOST", nullable = false, length = 255)
    private String imapHost;

    @Column(name = "IMAP_PORT", nullable = false)
    private Integer imapPort;

    @Column(name = "IMAP_USER", nullable = false, length = 255)
    private String imapUser;

    @Column(name = "IMAP_PASS", nullable = false, length = 255)
    private String imapPass;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive = true;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
