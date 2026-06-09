package com.connectit.core.dashboard.service;

import com.connectit.core.dashboard.dto.DashboardStatsDTO;
import com.connectit.core.user.repository.UserRepository;
import com.connectit.core.department.repository.DepartmentRepository;
import com.connectit.core.team.repository.TeamRepository;
import com.connectit.core.ticket.repository.TicketRepository;
import com.connectit.core.incident.repository.IncidentRepository;
import com.connectit.core.problem.repository.ProblemRepository;
import com.connectit.core.change.repository.ChangeRepository;
import com.connectit.core.approval.repository.ApprovalRequestRepository;
import com.connectit.core.sla.repository.TicketSlaTrackingRepository;
import com.connectit.core.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Arrays;
import java.time.LocalDateTime;

@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private TicketSlaTrackingRepository ticketSlaTrackingRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private ChangeRepository changeRepository;

    @Autowired
    private ApprovalRequestRepository approvalRequestRepository;

    @Transactional(readOnly = true)
    public DashboardStatsDTO getStats() {
        String username = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username).orElse(null);
        Long userId = currentUser != null ? currentUser.getId() : -1L;

        long assigned = ticketRepository.countByAssignedUserId(userId);
        long created = ticketRepository.countByRequesterId(userId);
        long open = ticketRepository.countByStatus("OPEN");
        long inProgress = ticketRepository.countByStatus("IN_PROGRESS");
        long resolved = ticketRepository.countByStatus("RESOLVED");
        long closed = ticketRepository.countByStatus("CLOSED");
        long pending = ticketRepository.countByStatusIn(Arrays.asList("NEW", "ON_HOLD"));

        // Overdue: not resolved/closed, and created more than 2 days ago
        long overdue = ticketRepository.findAll().stream()
                .filter(t -> !t.getStatus().equals("RESOLVED") && !t.getStatus().equals("CLOSED"))
                .filter(t -> t.getCreatedAt() != null && t.getCreatedAt().isBefore(LocalDateTime.now().minusDays(2)))
                .count();

        long breaches = ticketSlaTrackingRepository.countBreachedSlas();

        return DashboardStatsDTO.builder()
                .totalUsers(userRepository.count())
                .totalDepartments(departmentRepository.count())
                .totalTeams(teamRepository.count())
                .totalTickets(ticketRepository.count())
                .totalIncidents(incidentRepository.count())
                .totalProblems(problemRepository.count())
                .totalChanges(changeRepository.count())
                .totalApprovals(approvalRequestRepository.count())
                .tenantName("ConnectIT Corp")
                .totalIncidentsAssigned(assigned)
                .totalIncidentsCreated(created)
                .openIncidents(open)
                .inProgressIncidents(inProgress)
                .resolvedIncidents(resolved)
                .closedIncidents(closed)
                .pendingIncidents(pending)
                .overdueIncidents(overdue)
                .totalSlaBreaches(breaches)
                .build();
    }
}
