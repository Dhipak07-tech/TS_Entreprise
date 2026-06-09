package com.connectit.core.request.service;

import com.connectit.core.request.entity.ServiceCatalogItem;
import com.connectit.core.request.entity.ServiceRequest;
import com.connectit.core.request.repository.ServiceCatalogItemRepository;
import com.connectit.core.request.repository.ServiceRequestRepository;
import com.connectit.core.ticket.entity.Ticket;
import com.connectit.core.ticket.repository.TicketRepository;
import com.connectit.core.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class ServiceCatalogService {

    @Autowired
    private ServiceCatalogItemRepository serviceCatalogItemRepository;

    @Autowired
    private ServiceRequestRepository serviceRequestRepository;

    @Autowired
    private TicketRepository ticketRepository;

    public List<ServiceCatalogItem> getActiveCatalog() {
        return serviceCatalogItemRepository.findByIsActive(true);
    }

    @Transactional
    public ServiceRequest raiseServiceRequest(Long itemId, Integer quantity, User requester) {
        ServiceCatalogItem item = serviceCatalogItemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Catalog item not found"));

        // Generate Ticket number for this request
        String ticketNumber = "REQ-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        // Create Ticket object to wrap the request
        Ticket ticket = Ticket.builder()
                .ticketNumber(ticketNumber)
                .title("Request for: " + item.getName())
                .description("Catalog Item request for " + quantity + " x " + item.getName())
                .status("NEW")
                .priority("LOW")
                .source("WEB")
                .requester(requester)
                .build();

        Ticket savedTicket = ticketRepository.save(ticket);

        BigDecimal totalCost = item.getCost().multiply(BigDecimal.valueOf(quantity));

        ServiceRequest request = ServiceRequest.builder()
                .ticket(savedTicket)
                .catalogItem(item)
                .quantity(quantity)
                .totalCost(totalCost)
                .build();

        return serviceRequestRepository.save(request);
    }
}
