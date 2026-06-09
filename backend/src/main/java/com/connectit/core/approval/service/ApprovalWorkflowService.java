package com.connectit.core.approval.service;

import com.connectit.core.approval.entity.ApprovalPolicy;
import com.connectit.core.approval.entity.ApprovalRequest;
import com.connectit.core.approval.entity.ApprovalStep;
import com.connectit.core.approval.repository.ApprovalPolicyRepository;
import com.connectit.core.approval.repository.ApprovalRequestRepository;
import com.connectit.core.change.entity.Change;
import com.connectit.core.change.repository.ChangeRepository;
import com.connectit.core.notification.service.NotificationService;
import com.connectit.core.rbac.entity.Role;
import com.connectit.core.ticket.entity.Ticket;
import com.connectit.core.ticket.repository.TicketRepository;
import com.connectit.core.user.entity.User;
import com.connectit.core.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ApprovalWorkflowService {

    @Autowired
    private ApprovalPolicyRepository approvalPolicyRepository;

    @Autowired
    private ApprovalRequestRepository approvalRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChangeRepository changeRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public ApprovalRequest submitForApproval(String entityType, Long entityId) {
        ApprovalPolicy policy = approvalPolicyRepository.findByEntityType(entityType)
                .orElseThrow(() -> new RuntimeException("No approval policy found for type: " + entityType));

        if (policy.getSteps().isEmpty()) {
            // Auto approve if no steps configured
            completeEntityApproval(entityType, entityId, true);
            return null;
        }

        // Setup first step
        ApprovalStep firstStep = policy.getSteps().get(0);
        Role targetRole = firstStep.getApproverRole();

        // Assign to a default user with the matching role
        User approver = userRepository.findAll().stream()
                .filter(u -> u.getRoles().contains(targetRole))
                .findFirst()
                .orElse(null);

        ApprovalRequest request = ApprovalRequest.builder()
                .policy(policy)
                .entityId(entityId)
                .approver(approver)
                .status("PENDING")
                .stepIndex(1)
                .build();

        ApprovalRequest saved = approvalRequestRepository.save(request);

        if (approver != null) {
            notificationService.sendNotification(
                    approver,
                    "New Approval Request",
                    "A new " + entityType + " requires your approval.",
                    "APPROVAL_REQUIRED"
            );
        }

        return saved;
    }

    @Transactional
    public ApprovalRequest makeDecision(Long requestId, String decision, String remarks, User reviewer) {
        ApprovalRequest request = approvalRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Approval request not found"));

        request.setStatus(decision);
        request.setRemarks(remarks);
        request.setDecidedAt(LocalDateTime.now());
        request.setApprover(reviewer);

        if ("APPROVED".equalsIgnoreCase(decision)) {
            ApprovalPolicy policy = request.getPolicy();
            int nextStepIndex = request.getStepIndex() + 1;

            if (nextStepIndex <= policy.getSteps().size()) {
                // Advance to next step
                ApprovalStep nextStep = policy.getSteps().get(nextStepIndex - 1);
                Role targetRole = nextStep.getApproverRole();

                User nextApprover = userRepository.findAll().stream()
                        .filter(u -> u.getRoles().contains(targetRole))
                        .findFirst()
                        .orElse(null);

                request.setStepIndex(nextStepIndex);
                request.setStatus("PENDING");
                request.setApprover(nextApprover);
                request.setRemarks(null);
                request.setDecidedAt(null);

                if (nextApprover != null) {
                    notificationService.sendNotification(
                            nextApprover,
                            "Approval Escalated",
                            "A " + policy.getEntityType() + " has been escalated to you for approval.",
                            "APPROVAL_REQUIRED"
                    );
                }
            } else {
                // All steps passed! Complete approval
                completeEntityApproval(policy.getEntityType(), request.getEntityId(), true);
            }
        } else {
            // Rejected
            completeEntityApproval(request.getPolicy().getEntityType(), request.getEntityId(), false);
        }

        return approvalRequestRepository.save(request);
    }

    private void completeEntityApproval(String entityType, Long entityId, boolean approved) {
        if ("CHANGE".equalsIgnoreCase(entityType)) {
            changeRepository.findById(entityId).ifPresent(change -> {
                change.setStatus(approved ? "SCHEDULED" : "CLOSED");
                changeRepository.save(change);
            });
        } else if ("REQUEST".equalsIgnoreCase(entityType)) {
            // Service requests are wrapped in Tickets
            ticketRepository.findById(entityId).ifPresent(ticket -> {
                ticket.setStatus(approved ? "OPEN" : "CANCELLED");
                ticketRepository.save(ticket);
            });
        }
    }
}
