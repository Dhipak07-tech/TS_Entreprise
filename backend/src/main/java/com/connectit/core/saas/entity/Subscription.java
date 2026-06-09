package com.connectit.core.saas.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "SUBSCRIPTIONS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Subscription {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "PLAN_TIER", nullable = false, length = 50)
    private String planTier; // FREE, STARTER, PROFESSIONAL, ENTERPRISE

    @Column(name = "STATUS", nullable = false, length = 50)
    private String status; // ACTIVE, TRIALING, PAST_DUE, CANCELED

    @Column(name = "BILLING_CYCLE", nullable = false, length = 50)
    private String billingCycle; // MONTHLY, ANNUAL

    @Column(name = "CURRENT_PERIOD_START", nullable = false)
    private LocalDateTime currentPeriodStart;

    @Column(name = "CURRENT_PERIOD_END", nullable = false)
    private LocalDateTime currentPeriodEnd;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
}
