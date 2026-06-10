package com.connectit.core.rbac.service;

import com.connectit.core.rbac.dto.RoleDTO;
import com.connectit.core.rbac.dto.UpdateRolePermissionsRequest;
import com.connectit.core.rbac.entity.Permission;
import com.connectit.core.rbac.entity.Role;
import com.connectit.core.rbac.repository.PermissionRepository;
import com.connectit.core.rbac.repository.RoleRepository;
import com.connectit.core.rbac.repository.ActionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private ActionRepository actionRepository;

    @Autowired
    private PermissionCache permissionCache;

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
                if (perm.getIsAllowed() && perm.getAction() != null && "VIEW".equalsIgnoreCase(perm.getAction().getCode())) {
                    permittedPages.add(perm.getPage());
                }
            }
        }
        return permittedPages.stream().collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<com.connectit.core.rbac.dto.RolePagePermissionDTO> getPermissionMatrix() {
        List<com.connectit.core.rbac.entity.RolePagePermission> allPerms = rolePagePermissionRepository.findAll();
        
        Map<String, List<com.connectit.core.rbac.entity.RolePagePermission>> grouped = allPerms.stream()
                .filter(p -> p.getRole() != null && p.getPage() != null && p.getAction() != null)
                .collect(Collectors.groupingBy(p -> p.getRole().getId() + "_" + p.getPage().getPageId()));

        List<com.connectit.core.rbac.dto.RolePagePermissionDTO> matrix = new java.util.ArrayList<>();
        for (var entry : grouped.entrySet()) {
            List<com.connectit.core.rbac.entity.RolePagePermission> perms = entry.getValue();
            if (perms.isEmpty()) continue;
            var first = perms.get(0);
            
            var dto = com.connectit.core.rbac.dto.RolePagePermissionDTO.builder()
                    .roleId(first.getRole().getId())
                    .pageId(first.getPage().getPageId())
                    .pageName(first.getPage().getName())
                    .canView(false)
                    .canCreate(false)
                    .canUpdate(false)
                    .canDelete(false)
                    .canApprove(false)
                    .canReject(false)
                    .canAssign(false)
                    .canImport(false)
                    .canExport(false)
                    .canPrint(false)
                    .canReportAccess(false)
                    .build();

            for (var p : perms) {
                if (p.getIsAllowed()) {
                    String action = p.getAction().getCode().toUpperCase();
                    switch (action) {
                        case "VIEW" -> dto.setCanView(true);
                        case "CREATE" -> dto.setCanCreate(true);
                        case "UPDATE" -> dto.setCanUpdate(true);
                        case "DELETE" -> dto.setCanDelete(true);
                        case "APPROVE" -> dto.setCanApprove(true);
                        case "REJECT" -> dto.setCanReject(true);
                        case "ASSIGN" -> dto.setCanAssign(true);
                        case "IMPORT" -> dto.setCanImport(true);
                        case "EXPORT" -> dto.setCanExport(true);
                        case "PRINT" -> dto.setCanPrint(true);
                        case "REPORT_ACCESS" -> dto.setCanReportAccess(true);
                    }
                }
            }
            matrix.add(dto);
        }
        return matrix;
    }

    @Transactional
    public void updatePermissionMatrix(List<com.connectit.core.rbac.dto.RolePagePermissionDTO> dtos) {
        List<com.connectit.core.rbac.entity.Action> actions = actionRepository.findAll();
        
        for (var dto : dtos) {
            var role = roleRepository.findById(dto.getRoleId())
                    .orElseThrow(() -> new RuntimeException("Role not found: " + dto.getRoleId()));
            var page = pageRepository.findById(dto.getPageId())
                    .orElseThrow(() -> new RuntimeException("Page not found: " + dto.getPageId()));

            saveOrUpdateActionPerm(role, page, actions, "VIEW", dto.getCanView());
            saveOrUpdateActionPerm(role, page, actions, "CREATE", dto.getCanCreate());
            saveOrUpdateActionPerm(role, page, actions, "UPDATE", dto.getCanUpdate());
            saveOrUpdateActionPerm(role, page, actions, "DELETE", dto.getCanDelete());
            saveOrUpdateActionPerm(role, page, actions, "APPROVE", dto.getCanApprove());
            saveOrUpdateActionPerm(role, page, actions, "REJECT", dto.getCanReject());
            saveOrUpdateActionPerm(role, page, actions, "ASSIGN", dto.getCanAssign());
            saveOrUpdateActionPerm(role, page, actions, "IMPORT", dto.getCanImport());
            saveOrUpdateActionPerm(role, page, actions, "EXPORT", dto.getCanExport());
            saveOrUpdateActionPerm(role, page, actions, "PRINT", dto.getCanPrint());
        }

        permissionCache.refresh();
    }

    private void saveOrUpdateActionPerm(Role role, com.connectit.core.rbac.entity.Page page, 
                                        List<com.connectit.core.rbac.entity.Action> actions, 
                                        String actionCode, Boolean isAllowed) {
        var action = actions.stream()
                .filter(a -> actionCode.equalsIgnoreCase(a.getCode()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Action not found: " + actionCode));

        var key = com.connectit.core.rbac.entity.RolePagePermissionKey.builder()
                .roleId(role.getId())
                .pageId(page.getPageId())
                .actionId(action.getId())
                .build();

        var entity = rolePagePermissionRepository.findById(key)
                .orElseGet(() -> com.connectit.core.rbac.entity.RolePagePermission.builder()
                        .id(key)
                        .role(role)
                        .page(page)
                        .action(action)
                        .build());

        entity.setIsAllowed(isAllowed != null && isAllowed);
        rolePagePermissionRepository.save(entity);
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

    @Autowired
    private com.connectit.core.rbac.repository.UserPagePermissionRepository userPagePermissionRepository;

    @Transactional
    public void updateUserPermissions(Long userId, List<com.connectit.core.rbac.dto.UserPagePermissionDTO> dtos) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found: " + userId));
        List<com.connectit.core.rbac.entity.Action> actions = actionRepository.findAll();

        for (var dto : dtos) {
            var page = pageRepository.findById(dto.getPageId())
                    .orElseThrow(() -> new RuntimeException("Page not found: " + dto.getPageId()));
            var action = actions.stream()
                    .filter(a -> dto.getActionCode().equalsIgnoreCase(a.getCode()))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Action not found: " + dto.getActionCode()));

            var key = com.connectit.core.rbac.entity.UserPagePermissionKey.builder()
                    .userId(userId)
                    .pageId(page.getPageId())
                    .actionId(action.getId())
                    .build();

            if (dto.getIsAllowed() == null) {
                userPagePermissionRepository.deleteById(key);
            } else {
                var entity = userPagePermissionRepository.findById(key)
                        .orElseGet(() -> com.connectit.core.rbac.entity.UserPagePermission.builder()
                                .id(key)
                                .user(user)
                                .page(page)
                                .action(action)
                                .build());
                entity.setIsAllowed(dto.getIsAllowed());
                userPagePermissionRepository.save(entity);
            }
        }
        permissionCache.refresh();
    }

    private RoleDTO mapToDTO(Role role) {
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
