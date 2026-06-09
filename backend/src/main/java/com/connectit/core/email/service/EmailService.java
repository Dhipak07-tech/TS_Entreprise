package com.connectit.core.email.service;

import com.connectit.core.email.entity.EmailInbox;
import com.connectit.core.email.entity.EmailLog;
import com.connectit.core.email.repository.EmailInboxRepository;
import com.connectit.core.email.repository.EmailLogRepository;
import com.connectit.core.ticket.entity.Ticket;
import com.connectit.core.ticket.repository.TicketRepository;
import com.connectit.core.user.entity.User;
import com.connectit.core.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class EmailService {
    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);

    @Autowired
    private EmailInboxRepository emailInboxRepository;

    @Autowired
    private EmailLogRepository emailLogRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Transactional
    public void logEmail(String direction, String sender, String recipient, String subject, String body, String status, String error) {
        EmailLog emailLog = EmailLog.builder()
                .direction(direction)
                .sender(sender)
                .recipient(recipient)
                .subject(subject)
                .bodyHtml(body)
                .status(status)
                .errorMessage(error)
                .sentAt(LocalDateTime.now())
                .build();
        emailLogRepository.save(emailLog);
    }

    // Schedule to fetch/simulate inbound emails every 2 minutes
    @Scheduled(cron = "0 */2 * * * ?")
    @Transactional
    public void fetchInboundEmails() {
        List<EmailInbox> inboxes = emailInboxRepository.findByIsActive(true);
        if (inboxes.isEmpty()) {
            return;
        }

        logger.info("Polling {} active email inboxes via IMAP...", inboxes.size());

        // For demo/simulation purposes, we check if there are any un-parsed emails
        // and automatically create tickets when mock emails are received.
        // Let's simulate receiving a support request email:
        User mockRequester = userRepository.findByUsername("employee_user").orElse(null);
        if (mockRequester != null) {
            String subject = "VPN connection drops after 10 minutes";
            String body = "Hi Support,\n\nMy VPN connection drops constantly after 10 minutes of usage. Can you check this?\n\nThanks,\nEmployee User";
            
            // Check if ticket with this subject already exists to prevent duplicate demo tickets
            boolean exists = ticketRepository.findAll().stream()
                    .anyMatch(t -> t.getTitle().equalsIgnoreCase(subject));

            if (!exists) {
                logger.info("Simulated email received from: {}", mockRequester.getEmail());

                // Create ticket
                String ticketNum = "INC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
                Ticket ticket = Ticket.builder()
                        .ticketNumber(ticketNum)
                        .title(subject)
                        .description(body)
                        .status("NEW")
                        .priority("MEDIUM")
                        .source("EMAIL")
                        .requester(mockRequester)
                        .build();

                ticketRepository.save(ticket);
                logEmail("INBOUND", mockRequester.getEmail(), "support@connectit.com", subject, body, "PARSED", null);
                logger.info("Ticket {} created successfully via inbound email parsing.", ticketNum);
            }
        }
    }
}
