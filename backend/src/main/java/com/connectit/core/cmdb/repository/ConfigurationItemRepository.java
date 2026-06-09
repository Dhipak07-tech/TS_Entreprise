package com.connectit.core.cmdb.repository;

import com.connectit.core.cmdb.entity.ConfigurationItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConfigurationItemRepository extends JpaRepository<ConfigurationItem, Long> {
}
