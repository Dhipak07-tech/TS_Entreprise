package com.connectit.core.rbac.controller;

import com.connectit.common.dto.ApiResponse;
import com.connectit.core.rbac.dto.RoleDTO;
import com.connectit.core.rbac.dto.UpdateRolePermissionsRequest;
import com.connectit.core.rbac.entity.Permission;
import com.connectit.core.rbac.service.RbacService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rbac")
@PreAuthorize("hasAuthority('MANAGE_SYSTEM')")
public class RbacController {

    @Autowired
    private RbacService rbacService;

    @GetMapping("/roles")
    public ResponseEntity<ApiResponse<List<RoleDTO>>> getAllRoles() {
        return ResponseEntity.ok(ApiResponse.success(rbacService.getAllRoles()));
    }

    @GetMapping("/permissions")
    public ResponseEntity<ApiResponse<List<Permission>>> getAllPermissions() {
        return ResponseEntity.ok(ApiResponse.success(rbacService.getAllPermissions()));
    }

    @GetMapping("/menu")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<com.connectit.core.rbac.entity.Page>>> getPermittedMenu() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        com.connectit.config.security.services.UserDetailsImpl userDetails = (com.connectit.config.security.services.UserDetailsImpl) auth.getPrincipal();
        return ResponseEntity.ok(ApiResponse.success(rbacService.getPermittedMenu(userDetails.getId())));
    }

    @GetMapping("/matrix")
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<List<com.connectit.core.rbac.dto.RolePagePermissionDTO>>> getPermissionMatrix() {
        return ResponseEntity.ok(ApiResponse.success(rbacService.getPermissionMatrix()));
    }

    @PutMapping("/matrix")
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<Void>> updatePermissionMatrix(
            @RequestBody List<com.connectit.core.rbac.dto.RolePagePermissionDTO> request) {
        rbacService.updatePermissionMatrix(request);
        return ResponseEntity.ok(ApiResponse.success("Page permission matrix updated successfully", null));
    }

    @Autowired
    private com.connectit.core.rbac.service.PermissionService permissionService;

    @GetMapping("/menu-configurations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<com.connectit.core.rbac.entity.MenuConfiguration>>> getPermittedMenuConfigurations() {
        org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        com.connectit.config.security.services.UserDetailsImpl userDetails = (com.connectit.config.security.services.UserDetailsImpl) auth.getPrincipal();
        Long companyId = com.connectit.config.tenant.TenantContext.getCurrentTenant();
        if (companyId == null) {
            companyId = 1L;
        }
        return ResponseEntity.ok(ApiResponse.success(permissionService.getPermittedMenu(userDetails.getId(), companyId)));
    }

    @PutMapping("/user/{userId}/permissions")
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<Void>> updateUserPermissions(
            @PathVariable Long userId,
            @RequestBody List<com.connectit.core.rbac.dto.UserPagePermissionDTO> request) {
        rbacService.updateUserPermissions(userId, request);
        return ResponseEntity.ok(ApiResponse.success("User custom permissions updated successfully", null));
    }

    @PutMapping("/roles/{id}/permissions")
    @PreAuthorize("hasAuthority('MANAGE_SYSTEM')")
    public ResponseEntity<ApiResponse<RoleDTO>> updateRolePermissions(
            @PathVariable Long id,
            @RequestBody UpdateRolePermissionsRequest request) {
        RoleDTO updated = rbacService.updateRolePermissions(id, request);
        return ResponseEntity.ok(ApiResponse.success("Role permissions updated successfully", updated));
    }
}
