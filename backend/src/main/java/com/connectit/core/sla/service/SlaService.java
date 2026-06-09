package com.connectit.core.sla.service;

import com.connectit.core.notification.service.NotificationService;
import com.connectit.core.sla.entity.SlaPolicy;
import com.connectit.core.sla.entity.TicketSlaTracking;
import com.connectit.core.sla.repository.SlaPolicyRepository;
import com.connectit.core.sla.repository.TicketSlaTrackingRepository;
import com.connectit.core.ticket.entity.Ticket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SlaService {
    private static final Logger logger = LoggerFactory.getLogger(SlaService.class);

    @Autowired
    private SlaPolicyRepository slaPolicyRepository;

    @Autowired
    private TicketSlaTrackingRepository ticketSlaTrackingRepository;

    @Autowired
    private NotificationService notificationService;

    @Transactional
    public TicketSlaTracking startSlaTracking(Ticket ticket) {
        String priority = ticket.getPriority();
        SlaPolicy policy = slaPolicyRepository.findByPriority(priority)
                .orElseGet(() -> SlaPolicy.builder()
                        .name("Default Policy")
                        .priority(priority)
                        .responseTimeMins(480) // 8 hrs
                        .resolutionTimeMins(1440) // 24 hrs
                        .build());

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime responseDeadline = now.plusMinutes(policy.getResponseTimeMins());
        LocalDateTime resolutionDeadline = now.plusMinutes(policy.getResolutionTimeMins());

        TicketSlaTracking tracking = TicketSlaTracking.builder()
                .ticket(ticket)
                .slaPolicy(policy)
                .responseDeadline(responseDeadline)
                .resolutionDeadline(resolutionDeadline)
                .isResponseBreached(false)
                .isResolutionBreached(false)
                .build();

        return ticketSlaTrackingRepository.save(tracking);
    }

    @Transactional
    public void recordResponse(Long ticketId) {
        ticketSlaTrackingRepository.findByTicketId(ticketId).ifPresent(tracking -> {
            if (tracking.getRespondedAt() == null) {
                LocalDateTime now = LocalDateTime.now();
                tracking.setRespondedAt(now);
                if (now.isAfter(tracking.getResponseDeadline())) {
                    tracking.setIsResponseBreached(true);
                }
                ticketSlaTrackingRepository.save(tracking);
            }
        });
    }

    @Transactional
    public void recordResolution(Long ticketId) {
        ticketSlaTrackingRepository.findByTicketId(ticketId).ifPresent(tracking -> {
            if (tracking.getResolvedAt() == null) {
                LocalDateTime now = LocalDateTime.now();
                tracking.setResolvedAt(now);
                if (now.isAfter(tracking.getResolutionDeadline())) {
                    tracking.setIsResolutionBreached(true);
                }
                ticketSlaTrackingRepository.save(tracking);
            }
        });
    }

    // Cron job to run every 1 minute to check for response/resolution breaches
    @Scheduled(cron = "0 * * * * ?")
    @Transactional
    public void checkSlaBreaches() {
        LocalDateTime now = LocalDateTime.now();

        // 1. Check Response Deadlines
        List<TicketSlaTracking> responseLaggards = ticketSlaTrackingRepository
                .findByResponseDeadlineBeforeAndIsResponseBreachedFalseAndRespondedAtIsNull(now);
        for (TicketSlaTracking tracking : responseLaggards) {
            tracking.setIsResponseBreached(true);
            ticketSlaTrackingRepository.save(tracking);

            // Notify assigned user or requester
            Ticket ticket = tracking.getTicket();
            if (ticket.getAssignedUser() != null) {
                notificationService.sendNotification(
                        ticket.getAssignedUser(),
                        "SLA Response Breach",
                        "Ticket " + ticket.getTicketNumber() + " has breached its response SLA deadline.",
                        "SLA_BREACH"
                );
            }
            logger.warn("Ticket {} breached response SLA!", ticket.getTicketNumber());
        }

        // 2. Check Resolution Deadlines
        List<TicketSlaTracking> resolutionLaggards = ticketSlaTrackingRepository
                .findByResolutionDeadlineBeforeAndIsResolutionBreachedFalseAndResolvedAtIsNull(now);
        for (TicketSlaTracking tracking : resolutionLaggards) {
            tracking.setIsResolutionBreached(true);
            ticketSlaTrackingRepository.save(tracking);

            // Notify assigned user or team lead
            Ticket ticket = tracking.getTicket();
            if (ticket.getAssignedUser() != null) {
                notificationService.sendNotification(
                        ticket.getAssignedUser(),
                        "SLA Resolution Breach",
                        "Ticket " + ticket.getTicketNumber() + " has breached its resolution SLA deadline.",
                        "SLA_BREACH"
                );
            }
            logger.warn("Ticket {} breached resolution SLA!", ticket.getTicketNumber());
        }
    }
}
