package com.connectit.core.rbac.service;

import com.connectit.core.rbac.dto.RoleDTO;
import com.connectit.core.rbac.dto.UpdateRolePermissionsRequest;
import com.connectit.core.rbac.entity.Permission;
import com.connectit.core.rbac.entity.Role;
import com.connectit.core.rbac.repository.PermissionRepository;
import com.connectit.core.rbac.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RbacService {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PermissionRepository permissionRepository;

    @Autowired
    private com.connectit.core.rbac.repository.RolePagePermissionRepository rolePagePermissionRepository;

    @Autowired
    private com.connectit.core.rbac.repository.PageRepository pageRepository;

    @Autowired
    private com.connectit.core.user.repository.UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<RoleDTO> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<Permission> getAllPermissions() {
        return permissionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<com.connectit.core.rbac.entity.Page> getPermittedMenu(Long userId) {
        com.connectit.core.user.entity.User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Set<com.connectit.core.rbac.entity.Page> permittedPages = new java.util.HashSet<>();
        for (var role : user.getRoles()) {
            List<com.connectit.core.rbac.entity.RolePagePermission> permissions = rolePagePermissionRepository.findByRoleId(role.getId());
            for (var perm : permissions) {
                if (perm.getCanView()) {
                    permittedPages.add(perm.getPage());
                }
            }
        }
        return permittedPages.stream().collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<com.connectit.core.rbac.dto.RolePagePermissionDTO> getPermissionMatrix() {
        return rolePagePermissionRepository.findAll().stream()
                .map(perm -> com.connectit.core.rbac.dto.RolePagePermissionDTO.builder()
                        .roleId(perm.getRole().getId())
                        .pageId(perm.getPage().getPageId())
                        .pageName(perm.getPage().getName())
                        .canView(perm.getCanView())
                        .canCreate(perm.getCanCreate())
                        .canUpdate(perm.getCanUpdate())
                        .canDelete(perm.getCanDelete())
                        .canApprove(perm.getCanApprove())
                        .canReject(perm.getCanReject())
                        .canAssign(perm.getCanAssign())
                        .canImport(perm.getCanImport())
                        .canExport(perm.getCanExport())
                        .canPrint(perm.getCanPrint())
                        .canReportAccess(perm.getCanReportAccess())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public void updatePermissionMatrix(List<com.connectit.core.rbac.dto.RolePagePermissionDTO> dtos) {
        for (var dto : dtos) {
            com.connectit.core.rbac.entity.RolePagePermissionKey key = com.connectit.core.rbac.entity.RolePagePermissionKey.builder()
                    .roleId(dto.getRoleId())
                    .pageId(dto.getPageId())
                    .build();
            
            com.connectit.core.rbac.entity.RolePagePermission entity = rolePagePermissionRepository.findById(key)
                    .orElseGet(() -> {
                        var role = roleRepository.findById(dto.getRoleId())
                                .orElseThrow(() -> new RuntimeException("Role not found: " + dto.getRoleId()));
                        var page = pageRepository.findById(dto.getPageId())
                                .orElseThrow(() -> new RuntimeException("Page not found: " + dto.getPageId()));
                        return com.connectit.core.rbac.entity.RolePagePermission.builder()
                                .id(key)
                                .role(role)
                                .page(page)
                                .build();
                    });

            entity.setCanView(dto.getCanView());
            entity.setCanCreate(dto.getCanCreate());
            entity.setCanUpdate(dto.getCanUpdate());
            entity.setCanDelete(dto.getCanDelete());
            entity.setCanApprove(dto.getCanApprove());
            entity.setCanReject(dto.getCanReject());
            entity.setCanAssign(dto.getCanAssign());
            entity.setCanImport(dto.getCanImport());
            entity.setCanExport(dto.getCanExport());
            entity.setCanPrint(dto.getCanPrint());
            entity.setCanReportAccess(dto.getCanReportAccess());

            rolePagePermissionRepository.save(entity);
        }
    }

    @Transactional
    public RoleDTO updateRolePermissions(Long roleId, UpdateRolePermissionsRequest request) {
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        if (role.getIsSystemDefault() && "SUPER_ADMIN".equalsIgnoreCase(role.getName())) {
            throw new RuntimeException("Cannot modify permissions for system default SUPER_ADMIN role");
        }

        Set<Permission> newPermissions = new HashSet<>();
        if (request.getPermissionKeys() != null) {
            for (String key : request.getPermissionKeys()) {
                Permission perm = permissionRepository.findByPermKey(key)
                        .orElseThrow(() -> new RuntimeException("Permission not found: " + key));
                newPermissions.add(perm);
            }
        }

        role.setPermissions(newPermissions);
        Role saved = roleRepository.save(role);

        return mapToDTO(saved);
    }

    private RoleDTO mapToDTO(Role role) {
        // Initialize lazy permissions collection
        Set<String> permKeys = role.getPermissions().stream()
                .map(Permission::getPermKey)
                .collect(Collectors.toSet());

        return RoleDTO.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .isSystemDefault(role.getIsSystemDefault())
                .permissions(permKeys)
                .build();
    }
}
