package com.connectit.core.change.controller;

import com.connectit.common.dto.ApiResponse;
import com.connectit.core.change.entity.Change;
import com.connectit.core.change.service.ChangeService;
import com.connectit.core.approval.service.ApprovalWorkflowService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/changes")
public class ChangeController {

    @Autowired
    private ChangeService changeService;

    @Autowired
    private ApprovalWorkflowService approvalWorkflowService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Change>>> getAllChanges() {
        return ResponseEntity.ok(ApiResponse.success(changeService.getAllChanges()));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('APPROVE_REQUESTS','MANAGE_USERS','MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<Change>> createChange(@RequestBody ChangeRequest request) {
        Change changeRequest = changeService.createChange(
                request.getTitle(),
                request.getDescription(),
                request.getChangeType(),
                request.getRiskLevel(),
                request.getRollbackPlan(),
                request.getTestPlan(),
                request.getPlannedStart(),
                request.getPlannedEnd(),
                request.getTicketIds()
        );

        // If Emergency or Normal, submit for approval automatically
        if ("NORMAL".equalsIgnoreCase(changeRequest.getChangeType()) || "EMERGENCY".equalsIgnoreCase(changeRequest.getChangeType())) {
            approvalWorkflowService.submitForApproval("CHANGE", changeRequest.getId());
        }

        return ResponseEntity.ok(ApiResponse.success("Change request submitted successfully", changeRequest));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority('APPROVE_REQUESTS','MANAGE_USERS','MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<Change>> updateStatus(@PathVariable Long id, @RequestParam String status) {
        Change changeRequest = changeService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Change status updated successfully", changeRequest));
    }

    @Data
    public static class ChangeRequest {
        private String title;
        private String description;
        private String changeType;
        private String riskLevel;
        private String rollbackPlan;
        private String testPlan;
        private LocalDateTime plannedStart;
        private LocalDateTime plannedEnd;
        private List<Long> ticketIds;
    }
}
