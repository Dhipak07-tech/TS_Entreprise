package com.connectit.core.grc.repository;

import com.connectit.core.grc.entity.SecurityIncident;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SecurityIncidentRepository extends JpaRepository<SecurityIncident, Long> {
    boolean existsByIncidentNumber(String incidentNumber);
    long countByStatus(String status);
}
