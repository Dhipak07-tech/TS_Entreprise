package com.connectit.core.knowledge.service;

import com.connectit.core.knowledge.entity.KbArticle;
import com.connectit.core.knowledge.entity.KbCategory;
import com.connectit.core.knowledge.repository.KbArticleRepository;
import com.connectit.core.knowledge.repository.KbCategoryRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class KbService {

    private final KbArticleRepository kbArticleRepository;
    private final KbCategoryRepository kbCategoryRepository;

    public KbService(KbArticleRepository kbArticleRepository, KbCategoryRepository kbCategoryRepository) {
        this.kbArticleRepository = kbArticleRepository;
        this.kbCategoryRepository = kbCategoryRepository;
    }

    public Page<KbArticle> getArticles(String status, Pageable pageable) {
        if (status != null && !status.isEmpty()) {
            return kbArticleRepository.findByStatus(status, pageable);
        }
        return kbArticleRepository.findAll(pageable);
    }

    public Page<KbArticle> searchArticles(String query, Pageable pageable) {
        return kbArticleRepository.searchPublishedArticles(query, pageable);
    }

    public KbArticle createArticle(KbArticle article) {
        return kbArticleRepository.save(article);
    }

    public KbCategory createCategory(KbCategory category) {
        return kbCategoryRepository.save(category);
    }

    public List<KbCategory> getAllCategories() {
        return kbCategoryRepository.findAll();
    }
}
