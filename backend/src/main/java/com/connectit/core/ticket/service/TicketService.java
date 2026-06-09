package com.connectit.core.ticket.service;

import com.connectit.core.notification.service.NotificationService;
import com.connectit.core.sla.service.SlaService;
import com.connectit.core.team.entity.Team;
import com.connectit.core.team.repository.TeamRepository;
import com.connectit.core.ticket.dto.CreateTicketRequest;
import com.connectit.core.ticket.dto.TicketResponse;
import com.connectit.core.ticket.entity.Ticket;
import com.connectit.core.ticket.repository.TicketRepository;
import com.connectit.core.user.entity.User;
import com.connectit.core.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.connectit.core.ticket.dto.TicketDetailDTO;
import com.connectit.core.sla.repository.TicketSlaTrackingRepository;
import com.connectit.core.sla.entity.TicketSlaTracking;
import com.connectit.core.user.entity.UserProfile;
import com.connectit.core.user.repository.UserProfileRepository;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TicketService {

    @Autowired
    private TicketRepository ticketRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private SlaService slaService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private TicketSlaTrackingRepository ticketSlaTrackingRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private TicketCommentService ticketCommentService;

    @Transactional
    public TicketResponse createTicket(CreateTicketRequest request, User requester) {
        String ticketNumber = "INC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Team team = null;
        if (request.getAssignedTeamId() != null) {
            team = teamRepository.findById(request.getAssignedTeamId()).orElse(null);
        }

        User agent = null;
        if (request.getAssignedUserId() != null) {
            agent = userRepository.findById(request.getAssignedUserId()).orElse(null);
        }

        Ticket ticket = Ticket.builder()
                .ticketNumber(ticketNumber)
                .title(request.getTitle())
                .description(request.getDescription())
                .status("NEW")
                .priority(request.getPriority())
                .source(request.getSource() != null ? request.getSource() : "WEB")
                .requester(requester)
                .assignedTeam(team)
                .assignedUser(agent)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);

        // Start SLA tracking
        slaService.startSlaTracking(savedTicket);

        // Notify Agent if assigned
        if (agent != null) {
            notificationService.sendNotification(
                    agent,
                    "New Ticket Assigned",
                    "Ticket " + ticketNumber + " has been assigned to you.",
                    "TICKET_ASSIGNED"
            );
        }

        return mapToResponse(savedTicket);
    }

    public List<TicketResponse> getAllTickets() {
        return ticketRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<TicketResponse> getMyTickets(User user, String status, String search) {
        // If user is admin/support, return all, otherwise just request list
        boolean isAgentOrAdmin = user.getRoles().stream()
                .anyMatch(r -> r.getName().equals("ADMINISTRATOR") || r.getName().equals("SUPPORT_AGENT") || r.getName().equals("SUPER_ADMIN"));

        List<Ticket> tickets;
        if (isAgentOrAdmin) {
            tickets = ticketRepository.findAll();
        } else {
            tickets = ticketRepository.findByRequesterIdOrderByCreatedAtDesc(user.getId());
        }

        return tickets.stream()
                .filter(t -> {
                    if (status != null && !status.isEmpty()) {
                        return t.getStatus().equalsIgnoreCase(status);
                    }
                    return true;
                })
                .filter(t -> {
                    if (search != null && !search.isEmpty()) {
                        String q = search.toLowerCase();
                        return t.getTitle().toLowerCase().contains(q)
                                || t.getDescription().toLowerCase().contains(q)
                                || t.getTicketNumber().toLowerCase().contains(q);
                    }
                    return true;
                })
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private User getCurrentUser() {
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof com.connectit.config.security.services.UserDetailsImpl) {
                com.connectit.config.security.services.UserDetailsImpl userDetails = (com.connectit.config.security.services.UserDetailsImpl) auth.getPrincipal();
                return userRepository.findById(userDetails.getId()).orElse(null);
            }
        } catch (Exception e) {
            // ignore
        }
        return null;
    }

    public TicketDetailDTO getTicketDetail(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        TicketSlaTracking slaTracking = ticketSlaTrackingRepository.findByTicketId(ticket.getId()).orElse(null);

        UserProfile requesterProfile = userProfileRepository.findByUserId(ticket.getRequester().getId()).orElse(null);
        String reqName = requesterProfile != null && (requesterProfile.getFirstName() != null || requesterProfile.getLastName() != null)
                ? (requesterProfile.getFirstName() + " " + (requesterProfile.getLastName() != null ? requesterProfile.getLastName() : "")).trim()
                : ticket.getRequester().getUsername();

        String assignedTeamName = ticket.getAssignedTeam() != null ? ticket.getAssignedTeam().getName() : null;
        String assignedUserName = null;
        if (ticket.getAssignedUser() != null) {
            User agent = ticket.getAssignedUser();
            UserProfile agentProfile = userProfileRepository.findByUserId(agent.getId()).orElse(null);
            assignedUserName = agentProfile != null && (agentProfile.getFirstName() != null || agentProfile.getLastName() != null)
                    ? (agentProfile.getFirstName() + " " + (agentProfile.getLastName() != null ? agentProfile.getLastName() : "")).trim()
                    : agent.getUsername();
        }

        LocalDateTime responseDeadline = slaTracking != null ? slaTracking.getResponseDeadline() : null;
        LocalDateTime resolutionDeadline = slaTracking != null ? slaTracking.getResolutionDeadline() : null;
        Boolean isResponseBreached = slaTracking != null ? slaTracking.getIsResponseBreached() : false;
        Boolean isResolutionBreached = slaTracking != null ? slaTracking.getIsResolutionBreached() : false;

        Long minutesRemaining = null;
        if (resolutionDeadline != null) {
            if (slaTracking.getResolvedAt() != null) {
                minutesRemaining = Duration.between(slaTracking.getResolvedAt(), resolutionDeadline).toMinutes();
            } else {
                minutesRemaining = Duration.between(LocalDateTime.now(), resolutionDeadline).toMinutes();
            }
        }

        return TicketDetailDTO.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .source(ticket.getSource())
                .requesterName(reqName)
                .requesterEmail(ticket.getRequester().getEmail())
                .assignedTeamName(assignedTeamName)
                .assignedUserName(assignedUserName)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .responseDeadline(responseDeadline)
                .resolutionDeadline(resolutionDeadline)
                .isResponseBreached(isResponseBreached)
                .isResolutionBreached(isResolutionBreached)
                .minutesUntilResolutionDeadline(minutesRemaining)
                .build();
    }

    @Transactional
    public TicketResponse updateTicketStatus(Long ticketId, String status) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        String oldStatus = ticket.getStatus();
        ticket.setStatus(status);
        Ticket saved = ticketRepository.save(ticket);

        // Log status change activity
        User actor = getCurrentUser();
        ticketCommentService.logActivity(saved, actor, "STATUS_CHANGE", oldStatus, status, 
                "Ticket status changed from " + oldStatus + " to " + status);

        // If ticket is resolved/closed, stop SLA tracking
        if ("RESOLVED".equals(status) || "CLOSED".equals(status)) {
            slaService.recordResolution(ticketId);
        }

        // Notify requester about updates
        notificationService.sendNotification(
                ticket.getRequester(),
                "Ticket Status Updated",
                "Your ticket " + ticket.getTicketNumber() + " is now " + status + ".",
                "TICKET_STATUS"
        );

        return mapToResponse(saved);
    }

    @Transactional
    public TicketResponse assignTicket(Long ticketId, Long teamId, Long agentId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        String oldTeamName = ticket.getAssignedTeam() != null ? ticket.getAssignedTeam().getName() : "None";
        String oldAgentName = ticket.getAssignedUser() != null ? ticket.getAssignedUser().getUsername() : "None";

        if (teamId != null) {
            Team team = teamRepository.findById(teamId).orElse(null);
            ticket.setAssignedTeam(team);
        } else {
            ticket.setAssignedTeam(null);
        }

        if (agentId != null) {
            User agent = userRepository.findById(agentId).orElse(null);
            ticket.setAssignedUser(agent);

            // Record response starting if this is the first agent response
            slaService.recordResponse(ticketId);

            // Notify agent
            notificationService.sendNotification(
                    agent,
                    "Ticket Assigned",
                    "Ticket " + ticket.getTicketNumber() + " has been assigned to you.",
                    "TICKET_ASSIGNED"
            );
        } else {
            ticket.setAssignedUser(null);
        }

        Ticket saved = ticketRepository.save(ticket);

        String newTeamName = saved.getAssignedTeam() != null ? saved.getAssignedTeam().getName() : "None";
        String newAgentName = saved.getAssignedUser() != null ? saved.getAssignedUser().getUsername() : "None";

        // Log assign activity
        User actor = getCurrentUser();
        ticketCommentService.logActivity(saved, actor, "ASSIGNED", 
                oldTeamName + " / " + oldAgentName, 
                newTeamName + " / " + newAgentName, 
                "Ticket assigned to team: " + newTeamName + ", agent: " + newAgentName);

        return mapToResponse(saved);
    }

    private TicketResponse mapToResponse(Ticket ticket) {
        return TicketResponse.builder()
                .id(ticket.getId())
                .ticketNumber(ticket.getTicketNumber())
                .title(ticket.getTitle())
                .description(ticket.getDescription())
                .status(ticket.getStatus())
                .priority(ticket.getPriority())
                .source(ticket.getSource())
                .requesterId(ticket.getRequester().getId())
                .requesterName(ticket.getRequester().getUsername())
                .assignedTeamId(ticket.getAssignedTeam() != null ? ticket.getAssignedTeam().getId() : null)
                .assignedTeamName(ticket.getAssignedTeam() != null ? ticket.getAssignedTeam().getName() : null)
                .assignedUserId(ticket.getAssignedUser() != null ? ticket.getAssignedUser().getId() : null)
                .assignedUserName(ticket.getAssignedUser() != null ? ticket.getAssignedUser().getUsername() : null)
                .createdAt(ticket.getCreatedAt())
                .updatedAt(ticket.getUpdatedAt())
                .build();
    }
}
