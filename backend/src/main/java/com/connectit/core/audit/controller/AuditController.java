package com.connectit.core.audit.controller;

import com.connectit.common.dto.ApiResponse;
import com.connectit.core.audit.dto.AuditLogDTO;
import com.connectit.core.audit.dto.LoginLogDTO;
import com.connectit.core.audit.service.AuditService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/audit")
@PreAuthorize("hasAuthority('MANAGE_SYSTEM')")
public class AuditController {

    @Autowired
    private AuditService auditService;

    @GetMapping("/activities")
    public ResponseEntity<ApiResponse<List<AuditLogDTO>>> getAuditLogs() {
        return ResponseEntity.ok(ApiResponse.success(auditService.getAuditLogs()));
    }

    @GetMapping("/logins")
    public ResponseEntity<ApiResponse<List<LoginLogDTO>>> getLoginLogs() {
        return ResponseEntity.ok(ApiResponse.success(auditService.getLoginLogs()));
    }
}
