package com.connectit.core.approval.repository;

import com.connectit.core.approval.entity.ApprovalStep;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalStepRepository extends JpaRepository<ApprovalStep, Long> {
    List<ApprovalStep> findByPolicyIdOrderByStepOrderAsc(Long policyId);
}
