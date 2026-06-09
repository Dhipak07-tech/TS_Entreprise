package com.connectit.core.vendor.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "VENDORS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vendor {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "COMPANY_NAME", nullable = false, length = 150)
    private String companyName;

    @Column(name = "CONTACT_NAME", length = 150)
    private String contactName;

    @Column(name = "EMAIL", length = 255)
    private String email;

    @Column(name = "PHONE", length = 50)
    private String phone;

    @Column(name = "STATUS", nullable = false, length = 50)
    private String status; // ACTIVE, INACTIVE, BLACKLISTED
}
