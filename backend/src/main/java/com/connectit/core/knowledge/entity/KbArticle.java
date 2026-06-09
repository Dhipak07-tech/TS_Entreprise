package com.connectit.core.knowledge.entity;

import com.connectit.core.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "KB_ARTICLES")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KbArticle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CATEGORY_ID", nullable = false)
    private KbCategory category;

    @Column(name = "TITLE", nullable = false, length = 255)
    private String title;

    @Column(name = "CONTENT", nullable = false, length = 4000)
    private String content;

    @Column(name = "STATUS", nullable = false, length = 50)
    private String status; // DRAFT, UNDER_REVIEW, PUBLISHED

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "AUTHOR_ID", nullable = false)
    private User author;

    @Column(name = "VIEW_COUNT", nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Column(name = "IS_PINNED", nullable = false)
    @Builder.Default
    private Boolean isPinned = false;

    @Column(name = "PUBLISHED_AT")
    private LocalDateTime publishedAt;

    @Column(name = "CREATED_AT", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "UPDATED_AT")
    private LocalDateTime updatedAt;
}
