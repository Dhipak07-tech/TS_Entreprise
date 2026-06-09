package com.connectit.core.request.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "SERVICE_CATALOG_ITEMS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ServiceCatalogItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    @Column(name = "DESCRIPTION", nullable = false, length = 1000)
    private String description;

    @Column(name = "CATEGORY", nullable = false, length = 100)
    private String category;

    @Column(name = "COST", nullable = false, precision = 18, scale = 2)
    private BigDecimal cost = BigDecimal.ZERO;

    @Column(name = "IS_ACTIVE", nullable = false)
    private Boolean isActive = true;
}
