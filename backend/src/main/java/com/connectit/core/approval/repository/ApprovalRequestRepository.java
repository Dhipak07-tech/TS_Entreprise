package com.connectit.core.approval.repository;

import com.connectit.core.approval.entity.ApprovalRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ApprovalRequestRepository extends JpaRepository<ApprovalRequest, Long> {
    List<ApprovalRequest> findByApproverIdAndStatus(Long approverId, String status);
    List<ApprovalRequest> findByEntityIdAndPolicyEntityType(Long entityId, String entityType);
}
