package com.solcho.bootcamp.blank.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * docs/db-schema.md 의 blank_question 테이블 (빈칸 채우기 문제).
 * sentence_template 안의 "{blank}" 위치를 프론트에서 &lt;input&gt; 으로 치환해 렌더링한다.
 */
@Entity
@Table(name = "blank_question")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlankQuestion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    @Column(name = "sentence_template", columnDefinition = "TEXT", nullable = false)
    private String sentenceTemplate;

    @Column(name = "answer", length = 200, nullable = false)
    private String answer;

    @Column(name = "score", nullable = false)
    private int score;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Builder
    private BlankQuestion(Long unitId, String sentenceTemplate, String answer, int score, int sortOrder) {
        this.unitId = unitId;
        this.sentenceTemplate = sentenceTemplate;
        this.answer = answer;
        this.score = score;
        this.sortOrder = sortOrder;
    }

    /** 대소문자·앞뒤 공백을 무시하고 정답과 비교한다(학습 편의). */
    public boolean isCorrect(String submitted) {
        if (submitted == null) {
            return false;
        }
        return this.answer.trim().equalsIgnoreCase(submitted.trim());
    }
}
