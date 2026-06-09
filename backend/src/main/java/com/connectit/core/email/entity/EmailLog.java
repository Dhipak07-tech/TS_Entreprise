package com.connectit.core.email.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "EMAIL_LOGS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmailLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "DIRECTION", nullable = false, length = 10)
    private String direction; // INBOUND / OUTBOUND

    @Column(name = "SENDER", nullable = false, length = 255)
    private String sender;

    @Column(name = "RECIPIENT", nullable = false, length = 255)
    private String recipient;

    @Column(name = "SUBJECT", length = 500)
    private String subject;

    @Column(name = "BODY_HTML", length = 4000)
    private String bodyHtml;

    @Column(name = "STATUS", nullable = false, length = 50)
    private String status; // SENT, FAILED, PARSED

    @Column(name = "ERROR_MESSAGE", length = 1000)
    private String errorMessage;

    @Column(name = "SENT_AT", nullable = false, updatable = false)
    private LocalDateTime sentAt = LocalDateTime.now();
}
