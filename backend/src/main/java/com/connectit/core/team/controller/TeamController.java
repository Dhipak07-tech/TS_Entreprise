package com.connectit.core.team.controller;

import com.connectit.common.dto.ApiResponse;
import com.connectit.core.team.dto.TeamRequest;
import com.connectit.core.team.entity.Team;
import com.connectit.core.team.service.TeamService;
import com.connectit.core.user.dto.UserResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<Team>>> getAllTeams() {
        return ResponseEntity.ok(ApiResponse.success(teamService.getAllTeams()));
    }

    @GetMapping("/department/{departmentId}")
    public ResponseEntity<ApiResponse<List<Team>>> getTeamsByDepartment(@PathVariable Long departmentId) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getTeamsByDepartment(departmentId)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Team>> getTeamById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getTeamById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<Team>> createTeam(@Valid @RequestBody TeamRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Team created successfully", teamService.createTeam(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<Team>> updateTeam(@PathVariable Long id, @Valid @RequestBody TeamRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Team updated successfully", teamService.updateTeam(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<Void>> deleteTeam(@PathVariable Long id) {
        teamService.deleteTeam(id);
        return ResponseEntity.ok(ApiResponse.success("Team deleted successfully", null));
    }

    @GetMapping("/{id}/members")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getTeamMembers(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(teamService.getTeamMembers(id)));
    }
}
