package com.solcho.bootcamp.blank;

import com.solcho.bootcamp.blank.entity.BlankQuestion;
import com.solcho.bootcamp.blank.repository.BlankQuestionRepository;
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
 * GENERAL 단원 몇 개에 빈칸 채우기 문제를 시드한다. (4단계)
 * sentence_template 의 "{blank}" 위치가 프론트에서 입력칸으로 치환된다.
 * 단원 시드 이후 실행되도록 @Order(2).
 */
@Configuration
@Order(2)
public class BlankDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(BlankDataInitializer.class);

    private record Q(String template, String answer) {}

    private static final List<Q> LINUX_QUESTIONS = List.of(
            new Q("리눅스에서 파일 권한을 변경하는 명령어는 {blank} 이다.", "chmod"),
            new Q("현재 작업 디렉터리의 경로를 출력하는 명령어는 {blank} 이다.", "pwd"),
            new Q("파일의 소유자를 변경하는 명령어는 {blank} 이다.", "chown"),
            new Q("숨김 파일까지 포함해 목록을 보려면 ls 명령에 {blank} 옵션을 붙인다.", "-a"),
            new Q("데비안/우분투 계열에서 패키지를 설치할 때 쓰는 명령은 {blank} 이다.", "apt")
    );

    private static final List<Q> SHELL_QUESTIONS = List.of(
            new Q("스크립트 첫 줄 #!/bin/bash 의 '#!' 기호를 {blank} 라고 부른다.", "shebang"),
            new Q("변수 값을 참조할 때 변수 이름 앞에 붙이는 기호는 {blank} 이다.", "$"),
            new Q("if 로 시작한 조건문은 {blank} 로 끝난다.", "fi"),
            new Q("for/while 반복문 블록은 {blank} 로 끝난다.", "done")
    );

    @Bean
    CommandLineRunner seedBlanks(UnitRepository unitRepository, BlankQuestionRepository questionRepository) {
        return args -> {
            int created = 0;
            created += seedIfAbsent(unitRepository, questionRepository, "linux", LINUX_QUESTIONS);
            created += seedIfAbsent(unitRepository, questionRepository, "shell", SHELL_QUESTIONS);
            if (created > 0) {
                log.info("빈칸 문제 시드 생성 완료: {}개", created);
            }
        };
    }

    private int seedIfAbsent(UnitRepository unitRepository, BlankQuestionRepository questionRepository,
                             String code, List<Q> questions) {
        Unit unit = unitRepository.findByCode(code).orElse(null);
        if (unit == null || questionRepository.existsByUnitId(unit.getId())) {
            return 0;
        }
        for (int i = 0; i < questions.size(); i++) {
            Q q = questions.get(i);
            questionRepository.save(BlankQuestion.builder()
                    .unitId(unit.getId())
                    .sentenceTemplate(q.template())
                    .answer(q.answer())
                    .score(10)
                    .sortOrder(i + 1)
                    .build());
        }
        return questions.size();
    }
}
