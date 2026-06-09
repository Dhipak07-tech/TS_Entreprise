package com.connectit.core.knowledge.repository;

import com.connectit.core.knowledge.entity.KbArticle;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface KbArticleRepository extends JpaRepository<KbArticle, Long> {
    Page<KbArticle> findByStatus(String status, Pageable pageable);

    @Query("SELECT a FROM KbArticle a WHERE (LOWER(a.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(a.content) LIKE LOWER(CONCAT('%', :query, '%'))) AND a.status = 'PUBLISHED'")
    Page<KbArticle> searchPublishedArticles(@Param("query") String query, Pageable pageable);
}
