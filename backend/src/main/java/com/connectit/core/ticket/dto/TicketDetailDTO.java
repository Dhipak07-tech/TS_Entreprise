package com.connectit.core.ticket.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketDetailDTO {
    private Long id;
    private String ticketNumber;
    private String title;
    private String description;
    private String status;
    private String priority;
    private String source;
    private String requesterName;
    private String requesterEmail;
    private String assignedTeamName;
    private String assignedUserName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // SLA tracking fields
    private LocalDateTime responseDeadline;
    private LocalDateTime resolutionDeadline;
    private Boolean isResponseBreached;
    private Boolean isResolutionBreached;
    private Long minutesUntilResolutionDeadline;
}
