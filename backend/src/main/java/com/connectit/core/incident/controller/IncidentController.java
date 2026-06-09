package com.connectit.core.incident.controller;

import com.connectit.common.dto.ApiResponse;
import com.connectit.core.incident.entity.Incident;
import com.connectit.core.incident.service.IncidentService;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    @Autowired
    private IncidentService incidentService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Incident>>> getAllIncidents() {
        return ResponseEntity.ok(ApiResponse.success(incidentService.getAllIncidents()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Incident>> createIncident(@RequestBody IncidentRequest request) {
        Incident incident = incidentService.createIncident(
                request.getImpact(),
                request.getUrgency(),
                request.getCategory(),
                request.getSubcategory(),
                request.getTicketIds()
        );
        return ResponseEntity.ok(ApiResponse.success("Incident created successfully", incident));
    }

    @Data
    public static class IncidentRequest {
        private String impact;
        private String urgency;
        private String category;
        private String subcategory;
        private List<Long> ticketIds;
    }
}
