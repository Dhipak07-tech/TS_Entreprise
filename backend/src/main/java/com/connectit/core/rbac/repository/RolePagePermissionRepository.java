package com.connectit.core.rbac.repository;

import com.connectit.core.rbac.entity.RolePagePermission;
import com.connectit.core.rbac.entity.RolePagePermissionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RolePagePermissionRepository extends JpaRepository<RolePagePermission, RolePagePermissionKey> {

    @Query("SELECT rpp FROM RolePagePermission rpp JOIN FETCH rpp.page JOIN FETCH rpp.action WHERE rpp.id.roleId = :roleId")
    List<RolePagePermission> findByRoleId(@Param("roleId") Long roleId);
}
