package com.connectit.core.problem.entity;

import com.connectit.core.incident.entity.Incident;
import com.connectit.core.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "PROBLEMS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Problem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TITLE", nullable = false, length = 255)
    private String title;

    @Column(name = "DESCRIPTION", nullable = false, length = 4000)
    private String description;

    @Column(name = "ROOT_CAUSE", length = 4000)
    private String rootCause;

    @Column(name = "WORKAROUND", length = 4000)
    private String workaround;

    @Column(name = "RESOLUTION", length = 4000)
    private String resolution;

    @Column(name = "STATUS", nullable = false, length = 50)
    private String status; // OPEN, INVESTIGATING, RESOLVED, CLOSED

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "OWNER_ID", foreignKey = @ForeignKey(name = "FK_PROBLEMS_USERS"))
    private User owner;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "PROBLEM_INCIDENTS",
            joinColumns = @JoinColumn(name = "PROBLEM_ID", foreignKey = @ForeignKey(name = "FK_PROBLEM_INCIDENTS_PROBLEMS")),
            inverseJoinColumns = @JoinColumn(name = "INCIDENT_ID", foreignKey = @ForeignKey(name = "FK_PROBLEM_INCIDENTS_INCIDENTS"))
    )
    private Set<Incident> incidents = new HashSet<>();
}
