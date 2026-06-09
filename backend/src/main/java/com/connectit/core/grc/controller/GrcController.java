package com.connectit.core.grc.controller;

import com.connectit.common.dto.ApiResponse;
import com.connectit.core.grc.entity.SecurityIncident;
import com.connectit.core.grc.service.GrcService;
import com.connectit.core.grc.repository.SecurityIncidentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/grc")
public class GrcController {

    private final GrcService grcService;
    private final SecurityIncidentRepository securityIncidentRepository;

    public GrcController(GrcService grcService, SecurityIncidentRepository securityIncidentRepository) {
        this.grcService = grcService;
        this.securityIncidentRepository = securityIncidentRepository;
    }

    @GetMapping("/security-incidents")
    public ResponseEntity<ApiResponse<Page<SecurityIncident>>> getSecurityIncidents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Security incidents retrieved successfully", grcService.getSecurityIncidents(pageable)));
    }

    @PostMapping("/security-incidents")
    public ResponseEntity<ApiResponse<SecurityIncident>> createSecurityIncident(@RequestBody SecurityIncident incident) {
        return ResponseEntity.ok(ApiResponse.success("Security incident reported successfully", grcService.createSecurityIncident(incident)));
    }

    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getStats() {
        long openIncidents = securityIncidentRepository.countByStatus("OPEN");
        long investigatingIncidents = securityIncidentRepository.countByStatus("INVESTIGATING");
        long totalIncidents = securityIncidentRepository.count();
        long resolvedIncidents = securityIncidentRepository.countByStatus("RESOLVED")
                + securityIncidentRepository.countByStatus("CLOSED");

        long complianceScore = totalIncidents > 0
                ? (resolvedIncidents * 100) / totalIncidents
                : 100;

        return ResponseEntity.ok(ApiResponse.success(Map.of(
                "openIncidents", openIncidents + investigatingIncidents,
                "vulnerabilities", 0L,  // Will be real when VULNERABILITIES table is added
                "complianceScore", complianceScore,
                "totalIncidents", totalIncidents
        )));
    }
}
