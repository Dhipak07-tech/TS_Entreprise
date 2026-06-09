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
public class TicketActivityDTO {
    private Long id;
    private Long ticketId;
    private String actorName;
    private String activityType;
    private String oldValue;
    private String newValue;
    private String description;
    private LocalDateTime occurredAt;
}
