package com.solcho.bootcamp.activity.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * docs/db-schema.md 의 activity_log 테이블 (잔디심기).
 * 사용자 × 날짜당 1행, 그날 발생한 학습 활동 횟수를 누적한다. UNIQUE(user_id, activity_date).
 */
@Entity
@Table(name = "activity_log",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "activity_date"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "activity_date", nullable = false)
    private LocalDate activityDate;

    @Column(name = "activity_count", nullable = false)
    private int activityCount;

    @Builder
    private ActivityLog(Long userId, LocalDate activityDate) {
        this.userId = userId;
        this.activityDate = activityDate;
        this.activityCount = 1;
    }

    public void increment() {
        this.activityCount += 1;
    }
}
