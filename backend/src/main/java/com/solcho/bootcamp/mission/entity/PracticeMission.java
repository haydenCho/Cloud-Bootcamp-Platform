package com.solcho.bootcamp.mission.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * docs/db-schema.md 의 practice_mission 테이블 (실습 미션).
 * verify_pattern(정답 검증용 정규식)은 절대 클라이언트에 노출하지 않는다(치팅 방지).
 */
@Entity
@Table(name = "practice_mission")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PracticeMission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** PRACTICE 타입 unit 에 연결되는 FK (unit.id). */
    @Column(name = "unit_id", nullable = false)
    private Long unitId;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "mission_type", length = 20, nullable = false)
    private MissionType missionType;

    @Column(name = "verify_pattern", length = 500, nullable = false)
    private String verifyPattern;

    @Column(name = "xp_reward", nullable = false)
    private int xpReward;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Builder
    private PracticeMission(Long unitId, String title, String description, MissionType missionType,
                           String verifyPattern, int xpReward, int sortOrder) {
        this.unitId = unitId;
        this.title = title;
        this.description = description;
        this.missionType = missionType;
        this.verifyPattern = verifyPattern;
        this.xpReward = xpReward;
        this.sortOrder = sortOrder;
    }
}
