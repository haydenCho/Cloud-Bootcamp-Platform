package com.solcho.bootcamp.mission.verify;

import com.solcho.bootcamp.mission.entity.MissionType;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
import org.springframework.stereotype.Component;

/**
 * SHELL 유형 검증기. 사용자가 입력한 셸 명령을 실제로 실행하지 않고,
 * verify_pattern(정규식)에 매칭되는지만 확인한다.
 */
@Component
public class ShellMissionVerifier implements MissionVerifier {

    @Override
    public boolean supports(MissionType type) {
        return type == MissionType.SHELL;
    }

    @Override
    public boolean verify(String verifyPattern, String input) {
        if (input == null || verifyPattern == null) {
            return false;
        }
        try {
            // 여러 공백을 하나로 정규화하고 앞뒤 공백 제거 → 입력 편차 흡수
            String normalized = input.trim().replaceAll("\\s+", " ");
            return Pattern.compile(verifyPattern).matcher(normalized).find();
        } catch (PatternSyntaxException e) {
            // 잘못 시드된 패턴이면 정답으로 처리하지 않는다.
            return false;
        }
    }
}
