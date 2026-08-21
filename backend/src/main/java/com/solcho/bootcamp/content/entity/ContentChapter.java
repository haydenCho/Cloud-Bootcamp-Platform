package com.solcho.bootcamp.content.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * docs/db-schema.md 의 content_chapter 테이블 (학습 본문의 챕터 단위).
 * content 1 : N content_chapter. 본문(body)은 HTML, 렌더링 전 프론트에서 sanitize 한다.
 * sort_order 로 챕터 순서를 정한다(1부터).
 */
@Entity
@Table(name = "content_chapter")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ContentChapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "content_id", nullable = false)
    private Long contentId;

    @Column(name = "title", length = 300, nullable = false)
    private String title;

    @Lob
    @Column(name = "body", nullable = false, columnDefinition = "LONGTEXT")
    private String body;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private ContentChapter(Long contentId, String title, String body, int sortOrder) {
        this.contentId = contentId;
        this.title = title;
        this.body = body;
        this.sortOrder = sortOrder;
    }
}
