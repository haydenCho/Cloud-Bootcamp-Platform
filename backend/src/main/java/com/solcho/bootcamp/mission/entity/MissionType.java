package com.solcho.bootcamp.mission.entity;

/**
 * 실습 미션 유형. db-schema.md 의 practice_mission.mission_type enum 과 일치.
 * 5단계에서는 SHELL 검증만 구현하고, 나머지 유형은 이후 단계에서 Verifier 를 추가한다.
 */
public enum MissionType {
    PYTHON,
    DB,
    DOCKER,
    K8S,
    SHELL
}
