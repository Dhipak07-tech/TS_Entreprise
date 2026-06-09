package com.connectit.core.dashboard.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsDTO {
    private long totalUsers;
    private long totalDepartments;
    private long totalTeams;
    private long totalTickets;
    private long totalIncidents;
    private long totalProblems;
    private long totalChanges;
    private long totalApprovals;
    private String tenantName;

    // Personal dashboard fields
    private long totalIncidentsAssigned;
    private long totalIncidentsCreated;
    private long openIncidents;
    private long inProgressIncidents;
    private long resolvedIncidents;
    private long closedIncidents;
    private long pendingIncidents;
    private long overdueIncidents;
    private long totalSlaBreaches;
}
