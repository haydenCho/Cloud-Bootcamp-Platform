package com.solcho.bootcamp.mission.dto;

import com.solcho.bootcamp.mission.entity.MissionType;

/**
 * 미션 목록 항목.
 * ⚠️ verify_pattern(정답 정규식)은 포함하지 않는다.
 * completed 는 로그인 사용자의 완료 여부(비로그인 시 항상 false).
 */
public record MissionResponse(
        Long id,
        String title,
        String description,
        MissionType missionType,
        int xpReward,
        int sortOrder,
        boolean completed
) {
}
