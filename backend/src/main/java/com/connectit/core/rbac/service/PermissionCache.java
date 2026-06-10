package com.connectit.core.rbac.service;

import com.connectit.core.rbac.entity.RolePagePermission;
import com.connectit.core.rbac.entity.UserPagePermission;
import com.connectit.core.rbac.repository.RolePagePermissionRepository;
import com.connectit.core.rbac.repository.UserPagePermissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PermissionCache {

    @Autowired
    private RolePagePermissionRepository rolePagePermissionRepository;

    @Autowired
    private UserPagePermissionRepository userPagePermissionRepository;

    // Key: "roleId:pageId:actionCode" -> value: isAllowed
    private final ConcurrentHashMap<String, Boolean> rolePermissions = new ConcurrentHashMap<>();

    // Key: "userId:pageId:actionCode" -> value: isAllowed
    private final ConcurrentHashMap<String, Boolean> userPermissions = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        refresh();
    }

    public synchronized void refresh() {
        rolePermissions.clear();
        userPermissions.clear();

        List<RolePagePermission> rppList = rolePagePermissionRepository.findAll();
        for (RolePagePermission rpp : rppList) {
            if (rpp.getRole() != null && rpp.getPage() != null && rpp.getAction() != null) {
                String key = rpp.getRole().getId() + ":" + rpp.getPage().getPageId() + ":" + rpp.getAction().getCode();
                rolePermissions.put(key, rpp.getIsAllowed());
            }
        }

        List<UserPagePermission> uppList = userPagePermissionRepository.findAll();
        for (UserPagePermission upp : uppList) {
            if (upp.getUser() != null && upp.getPage() != null && upp.getAction() != null) {
                String key = upp.getUser().getId() + ":" + upp.getPage().getPageId() + ":" + upp.getAction().getCode();
                userPermissions.put(key, upp.getIsAllowed());
            }
        }
    }

    public Boolean getRolePermission(Long roleId, Integer pageId, String actionCode) {
        String key = roleId + ":" + pageId + ":" + actionCode;
        return rolePermissions.getOrDefault(key, false);
    }

    public Boolean getUserOverride(Long userId, Integer pageId, String actionCode) {
        String key = userId + ":" + pageId + ":" + actionCode;
        return userPermissions.get(key); // returns null if no override
    }
}
