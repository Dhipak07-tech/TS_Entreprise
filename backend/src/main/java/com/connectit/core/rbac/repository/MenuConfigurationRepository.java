package com.connectit.core.rbac.repository;

import com.connectit.core.rbac.entity.MenuConfiguration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuConfigurationRepository extends JpaRepository<MenuConfiguration, Long> {

    @Query("SELECT mc FROM MenuConfiguration mc LEFT JOIN FETCH mc.children LEFT JOIN FETCH mc.page WHERE mc.company.id = :companyId AND mc.isActive = true ORDER BY mc.sortOrder ASC")
    List<MenuConfiguration> findByCompanyId(@Param("companyId") Long companyId);
}
