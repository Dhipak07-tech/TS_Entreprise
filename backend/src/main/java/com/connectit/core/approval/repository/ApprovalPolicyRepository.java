package com.connectit.core.approval.repository;

import com.connectit.core.approval.entity.ApprovalPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ApprovalPolicyRepository extends JpaRepository<ApprovalPolicy, Long> {
    Optional<ApprovalPolicy> findByEntityType(String entityType);
}
