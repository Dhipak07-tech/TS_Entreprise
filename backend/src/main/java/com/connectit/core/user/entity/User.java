package com.connectit.core.user.entity;

import com.connectit.core.department.entity.Department;
import com.connectit.core.rbac.entity.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "USERS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "USERNAME", nullable = false, unique = true, length = 100)
    private String username;

    @Column(name = "EMAIL", nullable = false, unique = true, length = 255)
    private String email;

    @Column(name = "PASSWORD_HASH", nullable = false, length = 255)
    private String passwordHash;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive = true;

    @Column(name = "MFA_SECRET", length = 128)
    private String mfaSecret;

    @Column(name = "MFA_ENABLED", nullable = false)
    private Boolean mfaEnabled = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DEPARTMENT_ID", foreignKey = @ForeignKey(name = "FK_USERS_DEPARTMENTS"))
    private Department department;

    @Column(name = "COMPANY_ID", nullable = false)
    private Long companyId = 1L;

    @Column(name = "EMPLOYEE_ID", unique = true)
    private Long employeeId;

    @Column(name = "PASSWORD_RESET_REQUIRED", nullable = false)
    private Boolean passwordResetRequired = false;

    @Builder.Default
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "USER_ROLES",
            joinColumns = @JoinColumn(name = "USER_ID", foreignKey = @ForeignKey(name = "FK_USER_ROLES_USERS")),
            inverseJoinColumns = @JoinColumn(name = "ROLE_ID", foreignKey = @ForeignKey(name = "FK_USER_ROLES_ROLES"))
    )
    private Set<Role> roles = new HashSet<>();

    @CreatedDate
    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
}
