package com.connectit.core.audit.service;

import com.connectit.core.audit.dto.AuditLogDTO;
import com.connectit.core.audit.dto.LoginLogDTO;
import com.connectit.core.audit.entity.AuditLog;
import com.connectit.core.audit.entity.LoginLog;
import com.connectit.core.audit.repository.AuditLogRepository;
import com.connectit.core.audit.repository.LoginLogRepository;
import com.connectit.core.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuditService {

    @Autowired
    private AuditLogRepository auditLogRepository;

    @Autowired
    private LoginLogRepository loginLogRepository;

    @Transactional(readOnly = true)
    public List<AuditLogDTO> getAuditLogs() {
        return auditLogRepository.findAllByOrderByTimestampDesc().stream()
                .map(this::mapToAuditDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<LoginLogDTO> getLoginLogs() {
        return loginLogRepository.findAll(Sort.by(Sort.Direction.DESC, "timestamp")).stream()
                .map(this::mapToLoginDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void logActivity(User user, String action, String entityName, Long entityId, String oldValues, String newValues) {
        AuditLog log = AuditLog.builder()
                .user(user)
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .oldValues(oldValues)
                .newValues(newValues)
                .companyId(user != null ? user.getCompanyId() : 1L)
                .timestamp(LocalDateTime.now())
                .build();
        auditLogRepository.save(log);
    }

    private AuditLogDTO mapToAuditDTO(AuditLog log) {
        return AuditLogDTO.builder()
                .id(log.getId())
                .username(log.getUser() != null ? log.getUser().getUsername() : "System")
                .userEmail(log.getUser() != null ? log.getUser().getEmail() : null)
                .action(log.getAction())
                .entityName(log.getEntityName())
                .entityId(log.getEntityId())
                .oldValues(log.getOldValues())
                .newValues(log.getNewValues())
                .timestamp(log.getTimestamp())
                .build();
    }

    private LoginLogDTO mapToLoginDTO(LoginLog log) {
        return LoginLogDTO.builder()
                .id(log.getId())
                .username(log.getUser().getUsername())
                .userEmail(log.getUser().getEmail())
                .ipAddress(log.getIpAddress())
                .userAgent(log.getUserAgent())
                .mfaVerified(log.getMfaVerified())
                .timestamp(log.getTimestamp())
                .build();
    }
}
