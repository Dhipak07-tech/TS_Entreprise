package com.connectit.core.audit.entity;

import com.connectit.core.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "AUDIT_LOGS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", foreignKey = @ForeignKey(name = "FK_AUDIT_LOGS_USERS"))
    private User user;

    @Column(name = "ACTION", nullable = false, length = 100)
    private String action;

    @Column(name = "ENTITY_NAME", nullable = false, length = 100)
    private String entityName;

    @Column(name = "ENTITY_ID", nullable = false)
    private Long entityId;

    @Column(name = "OLD_VALUES", length = 4000)
    private String oldValues;

    @Column(name = "NEW_VALUES", length = 4000)
    private String newValues;

    @Column(name = "COMPANY_ID", nullable = false)
    private Long companyId = 1L;

    @Column(name = "TIMESTAMP", nullable = false, updatable = false)
    private LocalDateTime timestamp = LocalDateTime.now();
}
