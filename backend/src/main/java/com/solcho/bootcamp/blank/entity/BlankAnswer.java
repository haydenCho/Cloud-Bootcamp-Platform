package com.solcho.bootcamp.blank.entity;

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
 * docs/db-schema.md 의 blank_answer 테이블 (사용자별 빈칸 답안 상태).
 * UNIQUE(user_id, blank_question_id). 페이지 재방문 시 복원용으로 마지막 입력값을 저장한다.
 */
@Entity
@Table(name = "blank_answer",
        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "blank_question_id"}))
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlankAnswer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "blank_question_id", nullable = false)
    private Long blankQuestionId;

    @Column(name = "user_answer", length = 200)
    private String userAnswer;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    @Column(name = "attempts", nullable = false)
    private int attempts;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Builder
    private BlankAnswer(Long userId, Long blankQuestionId, String userAnswer, boolean isCorrect) {
        this.userId = userId;
        this.blankQuestionId = blankQuestionId;
        this.userAnswer = userAnswer;
        this.isCorrect = isCorrect;
        this.attempts = 1;
    }

    /** 재제출 시 답안/정답여부 갱신 및 시도 횟수 증가. */
    public void resubmit(String userAnswer, boolean isCorrect) {
        this.userAnswer = userAnswer;
        this.isCorrect = isCorrect;
        this.attempts += 1;
    }
}
