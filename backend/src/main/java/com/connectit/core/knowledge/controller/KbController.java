package com.connectit.core.knowledge.controller;

import com.connectit.common.dto.ApiResponse;
import com.connectit.core.knowledge.entity.KbArticle;
import com.connectit.core.knowledge.entity.KbCategory;
import com.connectit.core.knowledge.service.KbService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
public class KbController {

    private final KbService kbService;

    public KbController(KbService kbService) {
        this.kbService = kbService;
    }

    @GetMapping("/articles")
    public ResponseEntity<ApiResponse<Page<KbArticle>>> getArticles(
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Articles retrieved successfully", kbService.getArticles(status, pageable)));
    }

    @GetMapping("/articles/search")
    public ResponseEntity<ApiResponse<Page<KbArticle>>> searchArticles(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success("Articles searched successfully", kbService.searchArticles(query, pageable)));
    }

    @PostMapping("/articles")
    public ResponseEntity<ApiResponse<KbArticle>> createArticle(@RequestBody KbArticle article) {
        return ResponseEntity.ok(ApiResponse.success("Article created successfully", kbService.createArticle(article)));
    }

    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<KbCategory>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success("Categories retrieved successfully", kbService.getAllCategories()));
    }

    @PostMapping("/categories")
    public ResponseEntity<ApiResponse<KbCategory>> createCategory(@RequestBody KbCategory category) {
        return ResponseEntity.ok(ApiResponse.success("Category created successfully", kbService.createCategory(category)));
    }
}
