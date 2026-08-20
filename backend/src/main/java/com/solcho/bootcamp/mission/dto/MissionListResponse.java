package com.solcho.bootcamp.mission.dto;

import java.util.List;

/**
 * GET /api/v1/units/{code}/missions 응답.
 * earnedXp/totalXp 는 저장하지 않고 조회 시점에 계산한다
 * (db-schema: 계산 가능한 값은 저장하지 않는 원칙).
 *   - totalXp: 해당 단원 전체 미션 xp_reward 합
 *   - earnedXp: 완료한 미션의 xp_reward 합
 */
public record MissionListResponse(
        List<MissionResponse> missions,
        int earnedXp,
        int totalXp
) {
}
