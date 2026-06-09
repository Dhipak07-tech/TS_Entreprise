package com.connectit.core.incident.service;

import com.connectit.core.incident.entity.Incident;
import com.connectit.core.incident.repository.IncidentRepository;
import com.connectit.core.ticket.entity.Ticket;
import com.connectit.core.ticket.repository.TicketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;

@Service
public class IncidentService {

    @Autowired
    private IncidentRepository incidentRepository;

    @Autowired
    private TicketRepository ticketRepository;

    @Transactional
    public Incident createIncident(String impact, String urgency, String category, String subcategory, List<Long> ticketIds) {
        List<Ticket> tickets = ticketRepository.findAllById(ticketIds);
        boolean isMajor = "HIGH".equalsIgnoreCase(impact) && "HIGH".equalsIgnoreCase(urgency);

        Incident incident = Incident.builder()
                .impact(impact)
                .urgency(urgency)
                .category(category)
                .subcategory(subcategory)
                .majorIncident(isMajor)
                .tickets(new HashSet<>(tickets))
                .build();

        return incidentRepository.save(incident);
    }

    public List<Incident> getAllIncidents() {
        return incidentRepository.findAll();
    }
}
