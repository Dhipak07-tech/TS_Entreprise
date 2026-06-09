package com.connectit.core.knowledge.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "KB_CATEGORIES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KbCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "NAME", nullable = false, length = 150)
    private String name;

    @Column(name = "DESCRIPTION", length = 500)
    private String description;

    @Column(name = "PARENT_ID")
    private Long parentId;
}
