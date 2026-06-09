package com.connectit.core.knowledge.repository;

import com.connectit.core.knowledge.entity.KbCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KbCategoryRepository extends JpaRepository<KbCategory, Long> {
}
