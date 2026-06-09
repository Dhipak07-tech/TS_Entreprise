package com.connectit.core.rbac.repository;

import com.connectit.core.rbac.entity.RolePagePermission;
import com.connectit.core.rbac.entity.RolePagePermissionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePagePermissionRepository extends JpaRepository<RolePagePermission, RolePagePermissionKey> {
    List<RolePagePermission> findByRoleId(Long roleId);
}
