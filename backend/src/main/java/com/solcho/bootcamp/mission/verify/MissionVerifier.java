package com.solcho.bootcamp.mission.verify;

import com.solcho.bootcamp.mission.entity.MissionType;

/**
 * mission_type 별 검증 전략.
 *
 * 새로운 실습 유형(PYTHON/DB/DOCKER/K8S ...)을 추가할 때는
 * 이 인터페이스를 구현한 @Component 를 하나 더 만들기만 하면 된다.
 * MissionVerifierRegistry 가 supports() 로 알맞은 구현체를 자동으로 골라준다.
 *
 * ⚠️ CLAUDE.md 원칙: 어떤 구현체도 사용자가 입력한 코드를 실제로 실행하지 않는다.
 *     정답 패턴(정규식/키워드) 검증만 수행한다.
 */
public interface MissionVerifier {

    boolean supports(MissionType type);

    /**
     * @param verifyPattern practice_mission.verify_pattern (정규식/키워드)
     * @param input         사용자가 제출한 입력
     * @return 정답 여부
     */
    boolean verify(String verifyPattern, String input);
}
