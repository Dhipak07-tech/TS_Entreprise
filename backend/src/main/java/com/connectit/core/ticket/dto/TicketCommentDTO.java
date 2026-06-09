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
public class TicketCommentDTO {
    private Long id;
    private Long ticketId;
    private String authorName;
    private String authorEmail;
    private String body;
    private Boolean isInternal;
    private LocalDateTime createdAt;
}
