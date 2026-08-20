package com.solcho.bootcamp.mission.dto;

/**
 * 미션 검증 응답.
 *   - correct: 정답 여부
 *   - completed: 이 미션이 (이번 제출로 또는 이전에) 완료 상태인지
 *   - xpReward: 이 미션의 보상 XP (성공 시 "+N XP" 표시용)
 */
public record VerifyResponse(
        boolean correct,
        boolean completed,
        int xpReward
) {
}
