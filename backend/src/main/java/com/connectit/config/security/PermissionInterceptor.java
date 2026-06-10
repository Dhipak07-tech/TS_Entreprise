package com.connectit.config.security;

import com.connectit.config.security.services.UserDetailsImpl;
import com.connectit.core.rbac.service.PermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class PermissionInterceptor implements HandlerInterceptor {

    @Autowired
    private PermissionService permissionService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        RequiresPermission requiresPermission = handlerMethod.getMethodAnnotation(RequiresPermission.class);
        if (requiresPermission == null) {
            requiresPermission = handlerMethod.getBeanType().getAnnotation(RequiresPermission.class);
        }

        if (requiresPermission != null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication == null || !authentication.isAuthenticated()) {
                throw new AccessDeniedException("User is not authenticated");
            }

            Object principal = authentication.getPrincipal();
            if (!(principal instanceof UserDetailsImpl)) {
                throw new AccessDeniedException("Invalid authentication principal");
            }

            UserDetailsImpl userDetails = (UserDetailsImpl) principal;
            boolean hasAccess = permissionService.hasPermission(
                    userDetails.getId(),
                    requiresPermission.pageId(),
                    requiresPermission.action()
            );

            if (!hasAccess) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.getWriter().write("Access Denied: You do not have the required permissions.");
                return false;
            }
        }

        return true;
    }
}
