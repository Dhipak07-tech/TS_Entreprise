package com.connectit.core.cmdb.entity;

import com.connectit.core.asset.entity.Asset;
import com.connectit.core.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CONFIGURATION_ITEMS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfigurationItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    @Column(name = "CI_TYPE", nullable = false, length = 100)
    private String ciType; // DATABASE, SERVER, ROUTER, WEB_SERVICE

    @Column(name = "IP_ADDRESS", length = 50)
    private String ipAddress;

    @Column(name = "ENVIRONMENT", nullable = false, length = 50)
    private String environment; // PROD, STAGE, DEV

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "OWNER_USER_ID")
    private User owner;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ASSET_ID")
    private Asset asset;
}
