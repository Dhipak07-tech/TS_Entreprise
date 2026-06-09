package com.connectit.core.asset.entity;

import com.connectit.core.vendor.entity.Vendor;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "ASSETS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Asset {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ASSET_TAG", nullable = false, unique = true, length = 100)
    private String assetTag;

    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    @Column(name = "SERIAL_NUMBER", length = 150)
    private String serialNumber;

    @Column(name = "MODEL", length = 150)
    private String model;

    @Column(name = "ASSET_TYPE", nullable = false, length = 50)
    private String assetType; // HARDWARE, SOFTWARE, LICENSE

    @Column(name = "STATUS", nullable = false, length = 50)
    private String status; // IN_STOCK, IN_USE, RETIRED, LOST, REPAIR

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "VENDOR_ID")
    private Vendor vendor;

    @Column(name = "PURCHASE_DATE")
    private LocalDate purchaseDate;

    @Column(name = "WARRANTY_EXPIRY")
    private LocalDate warrantyExpiry;

    @Column(name = "COST", nullable = false)
    private BigDecimal cost;
}
