package com.connectit.core.cmdb.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "CI_RELATIONSHIPS")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CiRelationship {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "PARENT_CI_ID", nullable = false)
    private ConfigurationItem parentCi;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CHILD_CI_ID", nullable = false)
    private ConfigurationItem childCi;

    @Column(name = "RELATIONSHIP_TYPE", nullable = false, length = 100)
    private String relationshipType; // DEPENDS_ON, RUNS_ON, INSTALLED_ON
}
