package com.connectit.core.approval.entity;

import com.connectit.core.rbac.entity.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "APPROVAL_STEPS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalStep {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POLICY_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_APPROVAL_STEPS_POLICIES"))
    private ApprovalPolicy policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APPROVER_ROLE_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_APPROVAL_STEPS_ROLES"))
    private Role approverRole;

    @Column(name = "STEP_ORDER", nullable = false)
    private Integer stepOrder;
}
