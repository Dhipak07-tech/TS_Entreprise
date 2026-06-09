package com.connectit.core.problem.service;

import com.connectit.core.incident.entity.Incident;
import com.connectit.core.incident.repository.IncidentRepository;
import com.connectit.core.problem.entity.Problem;
import com.connectit.core.problem.repository.ProblemRepository;
import com.connectit.core.user.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@Service
public class ProblemService {

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private IncidentRepository incidentRepository;

    @Transactional
    public Problem createProblem(String title, String description, User owner, List<Long> incidentIds) {
        List<Incident> incidents = incidentRepository.findAllById(incidentIds);

        Problem problem = Problem.builder()
                .title(title)
                .description(description)
                .status("OPEN")
                .owner(owner)
                .createdAt(LocalDateTime.now())
                .incidents(new HashSet<>(incidents))
                .build();

        return problemRepository.save(problem);
    }

    @Transactional
    public Problem updateInvestigation(Long id, String rootCause, String workaround, String status) {
        Problem problem = problemRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Problem record not found"));

        problem.setRootCause(rootCause);
        problem.setWorkaround(workaround);
        problem.setStatus(status);

        return problemRepository.save(problem);
    }

    public List<Problem> getAllProblems() {
        return problemRepository.findAll();
    }
}
