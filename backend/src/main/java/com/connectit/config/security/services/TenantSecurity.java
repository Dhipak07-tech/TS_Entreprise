package com.connectit.config.security.services;

import com.connectit.config.tenant.TenantContext;
import com.connectit.core.audit.service.AuditService;
import com.connectit.core.rbac.entity.RolePagePermission;
import com.connectit.core.rbac.repository.RolePagePermissionRepository;
import com.connectit.core.user.entity.User;
import com.connectit.core.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("tenantSecurity")
public class TenantSecurity {

    @Autowired
    private RolePagePermissionRepository rolePagePermissionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuditService auditService;

    public boolean hasPagePermission(int pageId, String action) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof UserDetailsImpl)) {
            return false;
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) principal;
        Long userId = userDetails.getId();

        // 1. Fetch user to check roles
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return false;
        }

        // 2. Check each role mapped page permissions
        boolean permitted = false;
        for (var role : user.getRoles()) {
            List<RolePagePermission> permissions = rolePagePermissionRepository.findByRoleId(role.getId());
            for (RolePagePermission perm : permissions) {
                if (perm.getPage().getPageId() == pageId) {
                    switch (action.toUpperCase()) {
                        case "VIEW":
                            if (perm.getCanView()) permitted = true;
                            break;
                        case "CREATE":
                            if (perm.getCanCreate()) permitted = true;
                            break;
                        case "UPDATE":
                            if (perm.getCanUpdate()) permitted = true;
                            break;
                        case "DELETE":
                            if (perm.getCanDelete()) permitted = true;
                            break;
                        case "APPROVE":
                            if (perm.getCanApprove()) permitted = true;
                            break;
                        case "REJECT":
                            if (perm.getCanReject()) permitted = true;
                            break;
                        case "ASSIGN":
                            if (perm.getCanAssign()) permitted = true;
                            break;
                        case "IMPORT":
                            if (perm.getCanImport()) permitted = true;
                            break;
                        case "EXPORT":
                            if (perm.getCanExport()) permitted = true;
                            break;
                        case "PRINT":
                            if (perm.getCanPrint()) permitted = true;
                            break;
                        case "REPORT":
                        case "REPORT_ACCESS":
                            if (perm.getCanReportAccess()) permitted = true;
                            break;
                    }
                }
            }
        }

        // Log the check to the Audit Service as requested
        try {
            auditService.logActivity(
                user,
                permitted ? "PERMISSION_CHECK_SUCCESS" : "PERMISSION_CHECK_FAILED",
                "PAGE",
                (long) pageId,
                null,
                "Action: " + action + ", Permitted: " + permitted
            );
        } catch (Exception e) {
            // Suppress log failure to avoid breaking main path
        }

        return permitted;
    }

    public boolean isCompanyMember(Long resourceCompanyId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }

        Object principal = auth.getPrincipal();
        if (!(principal instanceof UserDetailsImpl)) {
            return false;
        }

        UserDetailsImpl userDetails = (UserDetailsImpl) principal;
        
        // ULTRA_SUPER_ADMIN has unrestricted global access
        boolean isUltraAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ULTRA_SUPER_ADMIN"));
        if (isUltraAdmin) {
            return true;
        }

        return userDetails.getCompanyId().equals(resourceCompanyId);
    }
}
