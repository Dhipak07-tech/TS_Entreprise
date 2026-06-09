package com.connectit.core.approval.entity;

import com.connectit.core.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "APPROVAL_REQUESTS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApprovalRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "POLICY_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_APPROVAL_REQUESTS_POLICIES"))
    private ApprovalPolicy policy;

    @Column(name = "ENTITY_ID", nullable = false)
    private Long entityId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "APPROVER_ID", foreignKey = @ForeignKey(name = "FK_APPROVAL_REQUESTS_USERS"))
    private User approver;

    @Column(name = "STATUS", nullable = false, length = 50)
    private String status; // PENDING, APPROVED, REJECTED

    @Column(name = "STEP_INDEX", nullable = false)
    private Integer stepIndex = 1;

    @Column(name = "REMARKS", length = 1000)
    private String remarks;

    @Column(name = "DECIDED_AT")
    private LocalDateTime decidedAt;
}
