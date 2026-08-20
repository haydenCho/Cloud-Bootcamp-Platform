package com.solcho.bootcamp.progress.dto;

/**
 * GET /api/v1/progress 응답 항목 (GENERAL 단원별 학습 진도 요약).
 * 프론트의 기존 더미 shape( {type:'GENERAL', generalPercent, blankPercent} )과 맞춘다.
 *   - generalPercent: progress.scroll_percent (일반 학습 진도)
 *   - blankPercent:  해당 단원 빈칸 문제 중 맞힌 비율
 */
public record UnitProgressSummary(
        String unitCode,
        String type,          // 항상 "GENERAL"
        int generalPercent,
        int blankPercent
) {
}
