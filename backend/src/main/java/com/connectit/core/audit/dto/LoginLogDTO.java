package com.connectit.core.audit.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginLogDTO {
    private Long id;
    private String username;
    private String userEmail;
    private String ipAddress;
    private String userAgent;
    private Boolean mfaVerified;
    private LocalDateTime timestamp;
}
