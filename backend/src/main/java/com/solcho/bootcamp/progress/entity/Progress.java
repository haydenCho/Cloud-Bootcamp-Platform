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
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * docs/db-schema.md 의 progress 테이블 (일반 학습 스크롤 기반 진도).
 * UNIQUE(user_id, unit_id).
 */
@Entity
@Table(name = "progress", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "unit_id"}))
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    @Column(name = "scroll_percent", nullable = false)
    private int scrollPercent;

    @Column(name = "completed", nullable = false)
    private boolean completed;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private Progress(Long userId, Long unitId, int scrollPercent) {
        this.userId = userId;
        this.unitId = unitId;
        applyScroll(scrollPercent);
    }

    /**
     * 스크롤 진도 갱신. 진도가 뒤로 가지 않도록 기존값과의 최댓값을 유지하고,
     * 90% 이상 도달하면 완료 처리(한 번 완료되면 유지)한다.
     */
    public void applyScroll(int newScrollPercent) {
        int clamped = Math.max(0, Math.min(100, newScrollPercent));
        this.scrollPercent = Math.max(this.scrollPercent, clamped);
        if (!this.completed && this.scrollPercent >= 90) {
            this.completed = true;
            this.completedAt = LocalDateTime.now();
        }
    }
}
