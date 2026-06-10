package com.connectit.core.rbac.repository;

import com.connectit.core.rbac.entity.UserPagePermission;
import com.connectit.core.rbac.entity.UserPagePermissionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserPagePermissionRepository extends JpaRepository<UserPagePermission, UserPagePermissionKey> {

    @Query("SELECT upp FROM UserPagePermission upp JOIN FETCH upp.page JOIN FETCH upp.action WHERE upp.id.userId = :userId")
    List<UserPagePermission> findByUserId(@Param("userId") Long userId);
}
