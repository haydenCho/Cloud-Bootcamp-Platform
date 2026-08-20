package com.solcho.bootcamp.mission;

import com.solcho.bootcamp.mission.entity.MissionType;
import com.solcho.bootcamp.mission.entity.PracticeMission;
import com.solcho.bootcamp.mission.repository.PracticeMissionRepository;
import com.solcho.bootcamp.unit.entity.Unit;
import com.solcho.bootcamp.unit.repository.UnitRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * 리눅스 실습(linux-practice) 단원에 SHELL 미션을 시드한다. (5단계)
 * 단원 시드(UnitDataInitializer) 이후 실행되도록 @Bean 메서드에 @Order(2)를 둔다.
 * (주의: CommandLineRunner 실행 순서는 @Configuration 클래스가 아니라 @Bean 팩토리 메서드의 @Order 로 결정된다.)
 *
 * verify_pattern 은 입력을 "앞뒤 trim + 연속 공백 1칸" 으로 정규화한 값과 매칭된다(ShellMissionVerifier).
 * 나머지 실습 단원(쉘/서버/파이썬/DB/도커/K8s) 미션은 이후 단계에서 각각 추가한다.
 */
@Configuration
public class MissionDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(MissionDataInitializer.class);

    private record M(String title, String description, MissionType type, String pattern, int xp) {}

    private static final List<M> LINUX_MISSIONS = List.of(
            new M("숨김 파일까지 모두 표시",
                    "현재 디렉토리의 숨김 파일까지 모두 표시하는 명령어를 입력하세요.",
                    MissionType.SHELL,
                    "^ls (-[a-zA-Z]*a[a-zA-Z]*|--all)$",
                    50),
            new M("현재 경로 확인",
                    "현재 작업 디렉토리의 절대 경로를 출력하는 명령어를 입력하세요.",
                    MissionType.SHELL,
                    "^pwd$",
                    30),
            new M("스크립트에 실행 권한 부여",
                    "deploy.sh 파일에 실행 권한을 부여하는 명령어를 입력하세요. (예: +x 또는 755)",
                    MissionType.SHELL,
                    "^chmod (\\+x|[1357][0-7][0-7]) (\\./)?deploy\\.sh$",
                    100),
            new M("중첩 디렉토리 한 번에 생성",
                    "project/src/main 디렉토리를 중간 경로까지 한 번의 명령으로 생성하세요.",
                    MissionType.SHELL,
                    "^mkdir -p project/src/main/?$",
                    70)
    );

    @Bean
    @Order(2)
    CommandLineRunner seedMissions(UnitRepository unitRepository,
                                   PracticeMissionRepository missionRepository) {
        return args -> {
            Unit unit = unitRepository.findByCode("linux-practice").orElse(null);
            if (unit == null || missionRepository.existsByUnitId(unit.getId())) {
                return;
            }
            for (int i = 0; i < LINUX_MISSIONS.size(); i++) {
                M m = LINUX_MISSIONS.get(i);
                missionRepository.save(PracticeMission.builder()
                        .unitId(unit.getId())
                        .title(m.title())
                        .description(m.description())
                        .missionType(m.type())
                        .verifyPattern(m.pattern())
                        .xpReward(m.xp())
                        .sortOrder(i + 1)
                        .build());
            }
            log.info("리눅스 실습 미션 시드 생성 완료: {}개", LINUX_MISSIONS.size());
        };
    }
}
