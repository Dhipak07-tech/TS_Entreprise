package com.connectit.core.settings.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "SETTINGS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Setting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "SETTING_KEY", nullable = false, unique = true, length = 100)
    private String key;

    @Column(name = "SETTING_VALUE", nullable = false, length = 255)
    private String value;

    @Column(name = "DESCRIPTION", length = 255)
    private String description;
}
