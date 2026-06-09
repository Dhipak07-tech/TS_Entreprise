package com.connectit.core.grc.service;

import com.connectit.core.grc.entity.SecurityIncident;
import com.connectit.core.grc.repository.SecurityIncidentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GrcService {

    private final SecurityIncidentRepository securityIncidentRepository;

    public GrcService(SecurityIncidentRepository securityIncidentRepository) {
        this.securityIncidentRepository = securityIncidentRepository;
    }

    public Page<SecurityIncident> getSecurityIncidents(Pageable pageable) {
        return securityIncidentRepository.findAll(pageable);
    }

    public SecurityIncident createSecurityIncident(SecurityIncident incident) {
        if (incident.getIncidentNumber() == null || incident.getIncidentNumber().isEmpty()) {
            incident.setIncidentNumber("SEC-INC-" + (System.currentTimeMillis() % 100000));
        }
        if (incident.getStatus() == null) {
            incident.setStatus("OPEN");
        }
        return securityIncidentRepository.save(incident);
    }
}
