package com.connectit.core.approval.controller;

import com.connectit.common.dto.ApiResponse;
import com.connectit.config.security.services.UserDetailsImpl;
import com.connectit.core.approval.entity.ApprovalRequest;
import com.connectit.core.approval.repository.ApprovalRequestRepository;
import com.connectit.core.approval.service.ApprovalWorkflowService;
import com.connectit.core.user.entity.User;
import com.connectit.core.user.repository.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/approvals")
public class ApprovalController {

    @Autowired
    private ApprovalWorkflowService approvalWorkflowService;

    @Autowired
    private ApprovalRequestRepository approvalRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/pending")
    public ResponseEntity<ApiResponse<List<ApprovalRequest>>> getPendingApprovals(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponse.success(
                approvalRequestRepository.findByApproverIdAndStatus(userDetails.getId(), "PENDING")
        ));
    }

    @PostMapping("/{id}/decide")
    public ResponseEntity<ApiResponse<ApprovalRequest>> makeDecision(
            @PathVariable Long id,
            @RequestBody DecisionRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User reviewer = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Logged in user not found"));

        ApprovalRequest updated = approvalWorkflowService.makeDecision(
                id,
                request.getDecision(),
                request.getRemarks(),
                reviewer
        );
        return ResponseEntity.ok(ApiResponse.success("Decision recorded successfully", updated));
    }

    @Data
    public static class DecisionRequest {
        private String decision; // APPROVED, REJECTED
        private String remarks;
    }
}
