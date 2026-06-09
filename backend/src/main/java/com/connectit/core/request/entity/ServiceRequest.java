package com.connectit.core.request.entity;

import com.connectit.core.ticket.entity.Ticket;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "SERVICE_REQUESTS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TICKET_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_SERVICE_REQUESTS_TICKETS"))
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CATALOG_ITEM_ID", nullable = false, foreignKey = @ForeignKey(name = "FK_SERVICE_REQUESTS_ITEMS"))
    private ServiceCatalogItem catalogItem;

    @Column(name = "QUANTITY", nullable = false)
    private Integer quantity = 1;

    @Column(name = "TOTAL_COST", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalCost;
}
