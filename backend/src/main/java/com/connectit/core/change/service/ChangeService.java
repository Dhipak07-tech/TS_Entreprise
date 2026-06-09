package com.connectit.core.change.service;

import com.connectit.core.change.entity.Change;
import com.connectit.core.change.repository.ChangeRepository;
import com.connectit.core.ticket.entity.Ticket;
import com.connectit.core.ticket.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Service
public class ChangeService {

    @Autowired
    private ChangeRepository changeRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Transactional
    public Change createChange(String title, String description, String changeType, String riskLevel,
                               String rollbackPlan, String testPlan, LocalDateTime start, LocalDateTime end,
                               List<Long> ticketIds) {
        List<Ticket> tickets = ticketRepository.findAllById(ticketIds);

        // Determine initial status based on type
        // Emergency/Normal changes require CAB Approval, Standard changes are pre-approved
        String status = "STANDARD".equalsIgnoreCase(changeType) ? "SCHEDULED" : "CAB_APPROVAL";

        Change changeRequest = Change.builder()
                .title(title)
                .description(description)
                .changeType(changeType)
                .riskLevel(riskLevel)
                .status(status)
                .rollbackPlan(rollbackPlan)
                .testPlan(testPlan)
                .plannedStart(start)
                .plannedEnd(end)
                .createdAt(LocalDateTime.now())
                .tickets(new HashSet<>(tickets))
                .build();

        return changeRepository.save(changeRequest);
    }

    @Transactional
    public Change updateStatus(Long id, String status) {
        Change changeRequest = changeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Change request not found"));
        changeRequest.setStatus(status);
        return changeRepository.save(changeRequest);
    }

    public List<Change> getAllChanges() {
        return changeRepository.findAll();
    }
}
