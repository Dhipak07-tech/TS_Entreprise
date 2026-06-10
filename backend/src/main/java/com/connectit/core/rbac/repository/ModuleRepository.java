package com.connectit.core.rbac.repository;

import com.connectit.core.rbac.entity.Module;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ModuleRepository extends JpaRepository<Module, Long> {
    Optional<Module> findByCode(String code);
    List<Module> findByIsActiveTrueOrderBySortOrderAsc();
}
