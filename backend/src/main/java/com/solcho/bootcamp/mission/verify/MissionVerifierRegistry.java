package com.solcho.bootcamp.mission.verify;

import com.solcho.bootcamp.common.exception.ApiException;
import com.solcho.bootcamp.mission.entity.MissionType;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * mission_type 에 맞는 MissionVerifier 를 골라준다.
 * Spring 이 모든 MissionVerifier @Component 를 주입하므로, 새 유형은 구현체만 추가하면 자동 등록된다.
 */
@Component
public class MissionVerifierRegistry {

    private final List<MissionVerifier> verifiers;

    public MissionVerifierRegistry(List<MissionVerifier> verifiers) {
        this.verifiers = verifiers;
    }

    public MissionVerifier get(MissionType type) {
        return verifiers.stream()
                .filter(v -> v.supports(type))
                .findFirst()
                .orElseThrow(() -> ApiException.badRequest(
                        "아직 지원하지 않는 실습 유형입니다: " + type));
    }
}
