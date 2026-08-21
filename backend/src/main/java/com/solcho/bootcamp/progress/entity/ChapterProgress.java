package com.solcho.bootcamp.progress.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * docs/db-schema.md 의 chapter_progress 테이블 (챕터 열람 기록).
 * 8단계 개선: 스크롤 기반 progress 를 대체한다. 사용자가 챕터를 열람하면 1행이 생기고,
 * "방문한 챕터 수 / 전체 챕터 수" 로 단원 진도를 계산한다(저장된 퍼센트 없음).
 * UNIQUE(user_id, chapter_id).
 */
@Entity
@Table(name = "chapter_progress",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "chapter_id"}))
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChapterProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "chapter_id", nullable = false)
    private Long chapterId;

    @CreatedDate
    @Column(name = "visited_at", updatable = false)
    private LocalDateTime visitedAt;

    @Builder
    private ChapterProgress(Long userId, Long chapterId) {
        this.userId = userId;
        this.chapterId = chapterId;
    }
}
