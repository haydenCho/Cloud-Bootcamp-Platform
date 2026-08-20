package com.solcho.bootcamp.content;

import com.solcho.bootcamp.content.entity.Content;
import com.solcho.bootcamp.content.repository.ContentRepository;
import com.solcho.bootcamp.unit.entity.Unit;
import com.solcho.bootcamp.unit.repository.UnitRepository;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

/**
 * GENERAL 단원의 학습 본문(HTML)을 classpath({@code resources/content/*.html})에서 읽어 시드한다.
 * 본문을 Java 문자열 상수로 두지 않는 이유: 노트 원문을 온전히 옮기면 단원 하나가 JVM의
 * 문자열 상수 크기 제한(UTF-8 65535바이트)을 넘을 수 있어 text block로는 컴파일이 안 된다.
 *
 * 단원이 없으면 새로 만들고, 이미 있으면 본문이 파일 내용과 다를 때만 갱신한다(단순 문자열 비교).
 * 즉 최초 1회는 기존에 들어가 있던 짧은 본문을 실제 파일 내용으로 교체하고, 이후에는 파일과
 * DB 내용이 같아지므로 재기동해도 불필요한 갱신 없이 그대로 유지된다(idempotent).
 *
 * 단원 시드(UnitDataInitializer) 이후 실행되도록 @Bean 메서드에 @Order(2)를 둔다.
 * (주의: CommandLineRunner 실행 순서는 @Configuration 클래스가 아니라 @Bean 팩토리 메서드의 @Order 로 결정된다.
 *  클래스에 @Order 를 두면 무시되어, 단원이 아직 없는 상태에서 이 러너가 먼저 실행되며 조용히 아무것도
 *  만들지 않는 사고가 날 수 있다 — 실제로 완전히 빈 DB에서 겪은 문제라 재발 방지 차 남긴다.)
 */
@Configuration
public class ContentDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(ContentDataInitializer.class);

    private record Seed(String code, String title, String file) {}

    private static final Seed[] SEEDS = {
            new Seed("cloud-intro", "클라우드 개론", "cloud-intro.html"),
            new Seed("linux", "리눅스 기초 — 개념, 명령어, 네트워크", "linux.html"),
            new Seed("shell", "쉘 스크립트 (Shell Script)", "shell.html"),
            new Seed("python", "파이썬 기초 정리", "python.html"),
            new Seed("aws", "AWS 핵심 서비스", "aws.html"),
            new Seed("docker", "도커(Docker)", "docker.html"),
            new Seed("network", "네트워크 기초와 패킷 분석", "network.html"),
            new Seed("security", "해킹과 보안 — 공격 기법과 탐지·방어 이론", "security.html"),
            new Seed("k8s", "쿠버네티스(K8s) 실무 — 아키텍처, 설치, 운영 명령어, 네임스페이스", "k8s.html"),
            new Seed("etc", "알아두면 좋은 보충 지식", "etc.html"),
    };

    @Bean
    @Order(2)
    CommandLineRunner seedContents(UnitRepository unitRepository, ContentRepository contentRepository) {
        return args -> {
            int created = 0;
            int updated = 0;
            for (Seed seed : SEEDS) {
                int result = seedOrUpdate(unitRepository, contentRepository, seed);
                if (result > 0) created += result == 1 ? 1 : 0;
                if (result == 2) updated++;
            }
            if (created > 0) {
                log.info("학습 콘텐츠 시드 생성 완료: {}개", created);
            }
            if (updated > 0) {
                log.info("학습 콘텐츠 본문 갱신 완료: {}개", updated);
            }
        };
    }

    /** @return 0=변경 없음, 1=신규 생성, 2=기존 본문 갱신 */
    private int seedOrUpdate(UnitRepository unitRepository, ContentRepository contentRepository, Seed seed) {
        Unit unit = unitRepository.findByCode(seed.code()).orElse(null);
        if (unit == null) {
            return 0;
        }
        String body = loadBody(seed.file());
        Optional<Content> existing = contentRepository.findFirstByUnitIdOrderByIdAsc(unit.getId());
        if (existing.isEmpty()) {
            contentRepository.save(Content.builder()
                    .unitId(unit.getId())
                    .title(seed.title())
                    .body(body)
                    .build());
            return 1;
        }
        Content content = existing.get();
        if (!content.getBody().equals(body) || !content.getTitle().equals(seed.title())) {
            content.updateBody(seed.title(), body);
            contentRepository.save(content);
            return 2;
        }
        return 0;
    }

    private String loadBody(String fileName) {
        try {
            return StreamUtils.copyToString(
                    new ClassPathResource("content/" + fileName).getInputStream(),
                    StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new UncheckedIOException("학습 콘텐츠 파일을 읽지 못했습니다: " + fileName, e);
        }
    }
}
