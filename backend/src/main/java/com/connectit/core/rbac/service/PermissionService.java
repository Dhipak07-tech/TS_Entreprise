package com.connectit.core.rbac.service;

import com.connectit.core.rbac.entity.MenuConfiguration;
import com.connectit.core.rbac.repository.MenuConfigurationRepository;
import com.connectit.core.user.entity.User;
import com.connectit.core.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PermissionService {

    @Autowired
    private PermissionCache permissionCache;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MenuConfigurationRepository menuConfigurationRepository;

    @Autowired
    private com.connectit.core.rbac.repository.RolePagePermissionRepository rolePagePermissionRepository;

    @Autowired
    private com.connectit.core.rbac.repository.UserPagePermissionRepository userPagePermissionRepository;

    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, Integer pageId, String actionCode) {
        // 1. Check User Overrides first (Highest Priority)
        Boolean userOverride = permissionCache.getUserOverride(userId, pageId, actionCode);
        if (userOverride != null) {
            return userOverride;
        }

        // 2. Check Role-level permissions across all roles assigned to the user
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getRoles() == null) {
            return false;
        }

        for (var role : user.getRoles()) {
            if (permissionCache.getRolePermission(role.getId(), pageId, actionCode)) {
                return true; // If any role grants permission, allow it
            }
        }

        return false;
    }

    @Transactional(readOnly = true)
    public List<MenuConfiguration> getPermittedMenu(Long userId, Long companyId) {
        List<MenuConfiguration> allMenus = menuConfigurationRepository.findByCompanyId(companyId);
        
        // Filter menus based on VIEW permission
        return allMenus.stream()
                .filter(menu -> menu.getParent() == null) // Top-level menus
                .filter(menu -> menu.getPage() == null || hasPermission(userId, menu.getPage().getPageId(), "VIEW"))
                .map(menu -> {
                    // Filter child menus recursively
                    List<MenuConfiguration> permittedChildren = menu.getChildren().stream()
                            .filter(child -> child.getIsActive() && (child.getPage() == null || hasPermission(userId, child.getPage().getPageId(), "VIEW")))
                            .collect(Collectors.toList());
                    menu.setChildren(permittedChildren);
                    return menu;
                })
                .collect(Collectors.toList());
     }

    @Transactional(readOnly = true)
    public List<String> getFormattedPermissionsForUser(Long userId) {
        List<String> formatted = new java.util.ArrayList<>();
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return formatted;
        }

        for (var role : user.getRoles()) {
            List<com.connectit.core.rbac.entity.RolePagePermission> rppList = rolePagePermissionRepository.findByRoleId(role.getId());
            for (var rpp : rppList) {
                if (rpp.getIsAllowed() && rpp.getPage() != null && rpp.getAction() != null) {
                    formatted.add("PAGE_" + rpp.getPage().getPageId() + "_" + rpp.getAction().getCode().toUpperCase());
                }
            }
        }

        List<com.connectit.core.rbac.entity.UserPagePermission> uppList = userPagePermissionRepository.findByUserId(userId);
        for (var upp : uppList) {
            if (upp.getPage() != null && upp.getAction() != null) {
                String permissionString = "PAGE_" + upp.getPage().getPageId() + "_" + upp.getAction().getCode().toUpperCase();
                if (upp.getIsAllowed()) {
                    if (!formatted.contains(permissionString)) {
                        formatted.add(permissionString);
                    }
                } else {
                    formatted.remove(permissionString);
                }
            }
        }

        return formatted;
    }
}
