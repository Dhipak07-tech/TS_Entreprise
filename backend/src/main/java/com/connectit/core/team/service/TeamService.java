package com.connectit.core.team.service;

import com.connectit.core.department.entity.Department;
import com.connectit.core.department.service.DepartmentService;
import com.connectit.core.team.dto.TeamRequest;
import com.connectit.core.team.entity.Team;
import com.connectit.core.team.repository.TeamRepository;
import com.connectit.core.user.entity.User;
import com.connectit.core.user.entity.UserProfile;
import com.connectit.core.user.dto.UserResponse;
import com.connectit.core.user.repository.UserRepository;
import com.connectit.core.user.repository.UserProfileRepository;
import com.connectit.core.rbac.entity.Role;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TeamService {

    @Autowired
    private TeamRepository teamRepository;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    public List<Team> getAllTeams() {
        return teamRepository.findAll();
    }

    public List<Team> getTeamsByDepartment(Long departmentId) {
        return teamRepository.findByDepartmentId(departmentId);
    }

    public Team getTeamById(Long id) {
        return teamRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: Team not found."));
    }

    @Transactional
    public Team createTeam(TeamRequest request) {
        Department department = departmentService.getDepartmentById(request.getDepartmentId());

        User teamLead = null;
        if (request.getTeamLeadId() != null) {
            teamLead = userRepository.findById(request.getTeamLeadId())
                    .orElseThrow(() -> new RuntimeException("Error: Team lead user not found."));
        }

        Team team = Team.builder()
                .name(request.getName())
                .department(department)
                .teamLead(teamLead)
                .build();

        return teamRepository.save(team);
    }

    @Transactional
    public Team updateTeam(Long id, TeamRequest request) {
        Team team = getTeamById(id);
        Department department = departmentService.getDepartmentById(request.getDepartmentId());

        User teamLead = null;
        if (request.getTeamLeadId() != null) {
            teamLead = userRepository.findById(request.getTeamLeadId())
                    .orElseThrow(() -> new RuntimeException("Error: Team lead user not found."));
        }

        team.setName(request.getName());
        team.setDepartment(department);
        team.setTeamLead(teamLead);

        return teamRepository.save(team);
    }

    @Transactional
    public void deleteTeam(Long id) {
        if (!teamRepository.existsById(id)) {
            throw new RuntimeException("Error: Team not found.");
        }
        teamRepository.deleteById(id);
    }

    public List<UserResponse> getTeamMembers(Long teamId) {
        Team team = getTeamById(teamId);
        return userRepository.findByDepartmentId(team.getDepartment().getId()).stream().map(user -> {
            UserProfile profile = userProfileRepository.findByUserId(user.getId())
                    .orElse(new UserProfile());
            return UserResponse.builder()
                    .id(user.getId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .isActive(user.getIsActive())
                    .firstName(profile.getFirstName())
                    .lastName(profile.getLastName())
                    .phone(profile.getPhone())
                    .avatarUrl(profile.getAvatarUrl())
                    .preferredLanguage(profile.getPreferredLanguage())
                    .departmentName(user.getDepartment() != null ? user.getDepartment().getName() : null)
                    .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                    .build();
        }).collect(Collectors.toList());
    }
}
