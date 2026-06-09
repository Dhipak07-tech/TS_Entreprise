package com.connectit.core.problem.controller;

import com.connectit.common.dto.ApiResponse;
import com.connectit.config.security.services.UserDetailsImpl;
import com.connectit.core.problem.entity.Problem;
import com.connectit.core.problem.service.ProblemService;
import com.connectit.core.user.entity.User;
import com.connectit.core.user.repository.UserRepository;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/problems")
public class ProblemController {

    @Autowired
    private ProblemService problemService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<Problem>>> getAllProblems() {
        return ResponseEntity.ok(ApiResponse.success(problemService.getAllProblems()));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Problem>> createProblem(
            @RequestBody ProblemCreateRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        User owner = userRepository.findById(userDetails.getId())
                .orElseThrow(() -> new RuntimeException("Logged in user not found"));

        Problem problem = problemService.createProblem(
                request.getTitle(),
                request.getDescription(),
                owner,
                request.getIncidentIds()
        );
        return ResponseEntity.ok(ApiResponse.success("Problem record created successfully", problem));
    }

    @PutMapping("/{id}/investigation")
    public ResponseEntity<ApiResponse<Problem>> updateInvestigation(
            @PathVariable Long id,
            @RequestBody ProblemUpdateRequest request) {
        Problem problem = problemService.updateInvestigation(
                id,
                request.getRootCause(),
                request.getWorkaround(),
                request.getStatus()
        );
        return ResponseEntity.ok(ApiResponse.success("Problem investigation updated successfully", problem));
    }

    @Data
    public static class ProblemCreateRequest {
        private String title;
        private String description;
        private List<Long> incidentIds;
    }

    @Data
    public static class ProblemUpdateRequest {
        private String rootCause;
        private String workaround;
        private String status;
    }
}
