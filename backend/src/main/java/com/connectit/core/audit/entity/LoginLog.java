package com.connectit.core.audit.entity;

import com.connectit.core.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "LOGIN_LOGS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_LOGIN_LOGS_USERS"))
    private User user;

    @Column(name = "IP_ADDRESS", nullable = false, length = 50)
    private String ipAddress;

    @Column(name = "USER_AGENT", length = 255)
    private String userAgent;

    @Column(name = "MFA_VERIFIED", nullable = false)
    private Boolean mfaVerified = false;

    @Column(name = "COMPANY_ID", nullable = false)
    private Long companyId = 1L;

    @Column(name = "TIMESTAMP", nullable = false, updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}
