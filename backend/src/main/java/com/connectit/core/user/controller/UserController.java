package com.connectit.core.user.controller;

import com.connectit.common.dto.ApiResponse;
import com.connectit.core.user.dto.UserResponse;
import com.connectit.core.user.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        return ResponseEntity.ok(ApiResponse.success(userService.getAllUsers()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(userService.getUserById(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUserProfile(
            @PathVariable Long id,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) String avatarUrl,
            @RequestParam(required = false, defaultValue = "en") String preferredLanguage,
            @RequestParam(required = false) Long departmentId,
            @RequestBody(required = false) Set<String> roles) {
        UserResponse response = userService.updateUserProfile(id, firstName, lastName, phone, avatarUrl, preferredLanguage, departmentId, roles);
        return ResponseEntity.ok(ApiResponse.success("User profile updated successfully", response));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(
                request.username,
                request.email,
                request.password,
                request.firstName,
                request.lastName,
                request.roles
        );
        return ResponseEntity.ok(ApiResponse.success("User created successfully", response));
    }

    @PatchMapping("/{id}/toggle-status")
    @PreAuthorize("hasAuthority('MANAGE_USERS')")
    public ResponseEntity<ApiResponse<UserResponse>> toggleUserStatus(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success("User status toggled successfully", userService.toggleUserStatus(id)));
    }

    public static class CreateUserRequest {
        public String username;
        public String email;
        public String password;
        public String firstName;
        public String lastName;
        public Set<String> roles;
    }
}
