package com.connectit.core.user.service;

import com.connectit.core.department.entity.Department;
import com.connectit.core.department.service.DepartmentService;
import com.connectit.core.rbac.entity.Role;
import com.connectit.core.rbac.repository.RoleRepository;
import com.connectit.core.user.dto.UserResponse;
import com.connectit.core.user.entity.User;
import com.connectit.core.user.entity.UserProfile;
import com.connectit.core.user.repository.UserProfileRepository;
import com.connectit.core.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProfileRepository userProfileRepository;

    @Autowired
    private DepartmentService departmentService;

    @Autowired
    private RoleRepository roleRepository;

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(user -> {
            UserProfile profile = userProfileRepository.findByUserId(user.getId())
                    .orElse(new UserProfile());
            return mapToResponse(user, profile);
        }).collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElse(new UserProfile());
        return mapToResponse(user, profile);
    }

    @Transactional
    public UserResponse updateUserProfile(Long id, String firstName, String lastName, String phone, String avatarUrl, String preferredLanguage, Long departmentId, Set<String> rolesList) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseGet(() -> UserProfile.builder().user(user).build());

        profile.setFirstName(firstName);
        profile.setLastName(lastName);
        profile.setPhone(phone);
        profile.setAvatarUrl(avatarUrl);
        profile.setPreferredLanguage(preferredLanguage);
        userProfileRepository.save(profile);

        if (departmentId != null) {
            Department dept = departmentService.getDepartmentById(departmentId);
            user.setDepartment(dept);
        } else {
            user.setDepartment(null);
        }

        if (rolesList != null && !rolesList.isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (String rName : rolesList) {
                Role role = roleRepository.findByName(rName)
                        .orElseThrow(() -> new RuntimeException("Error: Role " + rName + " not found."));
                roles.add(role);
            }
            user.setRoles(roles);
        }

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser, profile);
    }

    @Transactional
    public UserResponse createUser(String username, String email, String password, String firstName, String lastName, Set<String> rolesList) {
        User user = User.builder()
                .username(username)
                .email(email)
                .passwordHash(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode(password))
                .isActive(true)
                .mfaEnabled(false)
                .companyId(1L)
                .build();

        if (rolesList != null && !rolesList.isEmpty()) {
            Set<Role> roles = new HashSet<>();
            for (String rName : rolesList) {
                Role role = roleRepository.findByName(rName)
                        .orElseThrow(() -> new RuntimeException("Error: Role " + rName + " not found."));
                roles.add(role);
            }
            user.setRoles(roles);
        } else {
            Role defaultRole = roleRepository.findByName("USER")
                    .orElseThrow(() -> new RuntimeException("Error: Default USER role not found."));
            user.setRoles(Set.of(defaultRole));
        }

        User savedUser = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(savedUser)
                .firstName(firstName)
                .lastName(lastName)
                .preferredLanguage("en")
                .build();
        userProfileRepository.save(profile);

        return mapToResponse(savedUser, profile);
    }

    @Transactional
    public UserResponse toggleUserStatus(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Error: User not found."));
        user.setIsActive(!user.getIsActive());
        User savedUser = userRepository.save(user);

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElse(new UserProfile());

        return mapToResponse(savedUser, profile);
    }

    private UserResponse mapToResponse(User user, UserProfile profile) {
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
    }
}
