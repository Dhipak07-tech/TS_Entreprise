package com.connectit.config.security;

import com.connectit.config.security.services.UserDetailsImpl;
import com.connectit.core.rbac.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component("permissionEvaluator")
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Autowired
    private PermissionService permissionService;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || targetDomainObject == null || permission == null) {
            return false;
        }

        Long userId = getUserId(authentication);
        if (userId == null) {
            return false;
        }

        try {
            Integer pageId = Integer.valueOf(targetDomainObject.toString());
            String actionCode = permission.toString();
            return permissionService.hasPermission(userId, pageId, actionCode);
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        return false;
    }

    private Long getUserId(Authentication authentication) {
        if (authentication.getPrincipal() instanceof UserDetailsImpl) {
            return ((UserDetailsImpl) authentication.getPrincipal()).getId();
        }
        return null;
    }
}
