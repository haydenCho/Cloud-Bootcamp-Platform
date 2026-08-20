package com.solcho.bootcamp.unit.dto;

import com.solcho.bootcamp.unit.entity.Unit;
import com.solcho.bootcamp.unit.entity.UnitType;

/**
 * 단원 응답. hasContent/blankCount/missionCount 는 /study 카드의 보조 정보용으로 조회 시점에 채운다
 * (로드맵은 이 필드를 무시). 저장하지 않고 계산.
 */
public record UnitResponse(
        Long id,
        String code,
        String name,
        String groupCode,
        UnitType type,
        String iconImagePath,
        int sortOrder,
        boolean hasContent,
        int blankCount,
        int missionCount
) {
    public static UnitResponse of(Unit unit, boolean hasContent, int blankCount, int missionCount) {
        return new UnitResponse(
                unit.getId(),
                unit.getCode(),
                unit.getName(),
                unit.getGroupCode(),
                unit.getType(),
                unit.getIconImagePath(),
                unit.getSortOrder(),
                hasContent,
                blankCount,
                missionCount
        );
    }
}
