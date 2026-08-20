package com.solcho.bootcamp.unit;

import com.solcho.bootcamp.unit.entity.Unit;
import com.solcho.bootcamp.unit.entity.UnitType;
import com.solcho.bootcamp.unit.repository.UnitRepository;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * 애플리케이션 시작 시 17개 학습 단원을 없으면(code 기준) 생성한다.
 * type 은 이름에 "(실습)"이 붙은 것을 PRACTICE, 나머지를 GENERAL 로 둔다.
 * group_code 는 같은 주제(linux, docker, k8s, shell, python 등)끼리 묶는다.
 * icon_image_path 는 프론트에서 바로 쓰는 웹 경로(/assets/imgs/roadmap/{code}.png)로 저장한다.
 *
 * content/blank/mission 시드가 단원 id 를 참조하므로 가장 먼저 실행되도록 @Bean 메서드에 @Order(1)을 둔다.
 * (주의: CommandLineRunner 실행 순서는 @Configuration 클래스가 아니라 @Bean 팩토리 메서드의 @Order 로 결정된다.)
 */
@Configuration
public class UnitDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(UnitDataInitializer.class);

    /** 시드 정의: code, name, groupCode, type */
    private record Seed(String code, String name, String groupCode, UnitType type) {}

    private static final List<Seed> SEEDS = List.of(
            new Seed("cloud-intro",            "클라우드 개론",   "cloud",    UnitType.GENERAL),
            new Seed("linux",                  "리눅스",          "linux",    UnitType.GENERAL),
            new Seed("linux-practice",         "리눅스(실습)",    "linux",    UnitType.PRACTICE),
            new Seed("shell",                  "쉘 스크립트",     "shell",    UnitType.GENERAL),
            new Seed("shell-practice",         "쉘 스크립트(실습)", "shell",  UnitType.PRACTICE),
            new Seed("server-build-practice",  "서버 구축(실습)", "server",   UnitType.PRACTICE),
            new Seed("python",                 "파이썬",          "python",   UnitType.GENERAL),
            new Seed("python-practice",        "파이썬(실습)",    "python",   UnitType.PRACTICE),
            new Seed("database-practice",      "데이터베이스(실습)", "database", UnitType.PRACTICE),
            new Seed("aws",                    "AWS",             "aws",      UnitType.GENERAL),
            new Seed("docker",                 "도커",            "docker",   UnitType.GENERAL),
            new Seed("docker-practice",        "도커(실습)",      "docker",   UnitType.PRACTICE),
            new Seed("network",                "네트워크",        "network",  UnitType.GENERAL),
            new Seed("security",               "보안",            "security", UnitType.GENERAL),
            new Seed("k8s",                    "K8s",             "k8s",      UnitType.GENERAL),
            new Seed("k8s-practice",           "K8s(실습)",       "k8s",      UnitType.PRACTICE),
            new Seed("etc",                    "그외",            "etc",      UnitType.GENERAL)
    );

    @Bean
    @Order(1)
    CommandLineRunner seedUnits(UnitRepository unitRepository) {
        return args -> {
            int created = 0;
            for (int i = 0; i < SEEDS.size(); i++) {
                Seed seed = SEEDS.get(i);
                if (unitRepository.existsByCode(seed.code())) {
                    continue;
                }
                unitRepository.save(Unit.builder()
                        .code(seed.code())
                        .name(seed.name())
                        .groupCode(seed.groupCode())
                        .type(seed.type())
                        .iconImagePath("/assets/imgs/roadmap/" + seed.code() + ".png")
                        .sortOrder(i + 1)
                        .build());
                created++;
            }
            if (created > 0) {
                log.info("학습 단원 시드 생성 완료: {}개", created);
            }
        };
    }
}
