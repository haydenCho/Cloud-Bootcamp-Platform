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
 * 단원 시드(UnitDataInitializer) 이후 실행되도록 @Bean 메서드에 @Order(2)를 둔다.
 * (주의: CommandLineRunner 실행 순서는 @Configuration 클래스가 아니라 @Bean 팩토리 메서드의 @Order 로 결정된다.)
 */
@Configuration
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

    private static final List<Q> CLOUD_QUESTIONS = List.of(
            new Q("물리 서버 위에서 여러 개의 가상 머신(VM)을 생성하고 관리하는 소프트웨어를 {blank}(이)라고 한다.", "하이퍼바이저"),
            new Q("서버리스 함수가 처음 실행될 때 함수를 불러오면서 발생하는 지연 시간을 {blank} 스타트라고 한다.", "콜드"),
            new Q("2006년 아마존은 스토리지 서비스인 {blank}와(과) 컴퓨팅 서비스인 EC2를 출시하며 인터넷 상에서 자원을 빌려 쓰는 시대를 열었다.", "S3"),
            new Q("완성된 소프트웨어 애플리케이션 자체를 인터넷을 통해 구독 형태로 제공하는 클라우드 서비스 모델을 {blank}(이)라고 한다.", "SaaS")
    );

    private static final List<Q> PYTHON_QUESTIONS = List.of(
            new Q("리스트는 값의 변경, 추가, 삭제가 자유로워 {blank} 자료형이라 부르며 대괄호로 생성한다.", "mutable"),
            new Q("튜플은 한 번 만들면 값을 바꿀 수 없어 {blank} 자료형이라 하며 소괄호로 만든다.", "immutable"),
            new Q("정수끼리 나눠도 나눗셈 연산자 /는 항상 {blank} 타입의 결과를 반환한다.", "float"),
            new Q("딕셔너리에 특정 키가 존재하는지 확인할 때는 {blank} 연산자를 사용한다.", "in")
    );

    private static final List<Q> AWS_QUESTIONS = List.of(
            new Q("가상 서버를 생성하여 애플리케이션을 실행하는 컴퓨팅 서비스는 {blank}이다.", "EC2"),
            new Q("파일을 버킷 단위로 저장하는 객체 스토리지 서비스는 {blank}이다.", "S3"),
            new Q("사용자와 권한, 역할을 관리하여 접근을 제어하는 서비스는 {blank}이다.", "IAM"),
            new Q("클라우드 안에서 논리적으로 격리된 가상 네트워크를 제공하는 서비스는 {blank}이다.", "VPC")
    );

    private static final List<Q> DOCKER_QUESTIONS = List.of(
            new Q("이미지를 내려받아 컨테이너를 생성하고 실행하는 것을 한 번에 하는 명령어는 docker {blank} 이다.", "run"),
            new Q("Dockerfile 레시피를 읽어 나만의 이미지를 만드는 명령어는 docker {blank} 이다.", "build"),
            new Q("컨테이너를 삭제해도 DB 데이터가 사라지지 않게 하려면 데이터를 {blank}에 저장해야 한다.", "volume"),
            new Q("여러 컨테이너를 YAML 파일 하나로 정의하고 명령 한 줄로 전부 실행하는 도구는 docker {blank} 이다.", "compose")
    );

    private static final List<Q> NETWORK_QUESTIONS = List.of(
            new Q("전송 계층에서 연결을 설정하고 재전송으로 신뢰성을 보장하는 프로토콜은 {blank}이다.", "TCP"),
            new Q("도메인 이름을 IP 주소로 변환해 주는 프로토콜은 {blank}이다.", "DNS"),
            new Q("웹 페이지를 주고받을 때 사용하며 80번 포트를 쓰는 프로토콜은 {blank}이다.", "HTTP"),
            new Q("신뢰성 보장 대신 속도와 가벼움을 우선하는 비연결형 전송 계층 프로토콜은 {blank}이다.", "UDP")
    );

    private static final List<Q> SECURITY_QUESTIONS = List.of(
            new Q("SHA-256처럼 원본 데이터로 되돌릴 수 없는 해시 방식을 {blank} 암호화라고 한다.", "단방향"),
            new Q("비밀번호는 평문으로 저장하지 않고 {blank} 같은 해시 알고리즘으로 저장한다.", "BCrypt"),
            new Q("평문 전송을 노리는 스니핑을 무력화하려면 통신을 암호화하는 {blank}를 적용한다.", "HTTPS"),
            new Q("등록된 특정 IP나 도메인만 접속을 허용하는 방화벽 규칙을 {blank}라고 한다.", "화이트리스트")
    );

    private static final List<Q> K8S_QUESTIONS = List.of(
            new Q("컨테이너를 실행하는 쿠버네티스의 가장 작은 배포 단위는 {blank} 이다.", "Pod"),
            new Q("ReplicaSet을 관리하며 배포, 롤백, 스케일링을 담당하는 오브젝트는 {blank} 이다.", "Deployment"),
            new Q("여러 파드에 하나의 고정된 접근 지점을 제공하고 트래픽을 로드밸런싱하는 오브젝트는 {blank} 이다.", "Service"),
            new Q("클러스터에 명령을 전달하는 가장 대표적인 커맨드 라인 도구는 {blank} 이다.", "kubectl")
    );

    private static final List<Q> ETC_QUESTIONS = List.of(
            new Q("변경 이력을 관리하는 분산 버전 관리 시스템으로, add·commit·push 흐름으로 협업하는 도구는 {blank} 이다.", "git"),
            new Q("민감한 설정 값을 소스에 직접 쓰지 않고 분리해 저장하며 보통 버전 관리에서 제외하는 파일은 {blank} 파일이다.", ".env"),
            new Q("들여쓰기로 구조를 표현하며 docker-compose 나 쿠버네티스 매니페스트에 주로 쓰이는 설정 포맷은 {blank} 이다.", "yaml"),
            new Q("코드를 자주 병합하며 자동으로 빌드·테스트하는 개발 관행을 영어 약자로 {blank} 라고 부른다.", "CI")
    );

    @Bean
    @Order(2)
    CommandLineRunner seedBlanks(UnitRepository unitRepository, BlankQuestionRepository questionRepository) {
        return args -> {
            int created = 0;
            created += seedIfAbsent(unitRepository, questionRepository, "linux", LINUX_QUESTIONS);
            created += seedIfAbsent(unitRepository, questionRepository, "shell", SHELL_QUESTIONS);
            created += seedIfAbsent(unitRepository, questionRepository, "cloud-intro", CLOUD_QUESTIONS);
            created += seedIfAbsent(unitRepository, questionRepository, "python", PYTHON_QUESTIONS);
            created += seedIfAbsent(unitRepository, questionRepository, "aws", AWS_QUESTIONS);
            created += seedIfAbsent(unitRepository, questionRepository, "docker", DOCKER_QUESTIONS);
            created += seedIfAbsent(unitRepository, questionRepository, "network", NETWORK_QUESTIONS);
            created += seedIfAbsent(unitRepository, questionRepository, "security", SECURITY_QUESTIONS);
            created += seedIfAbsent(unitRepository, questionRepository, "k8s", K8S_QUESTIONS);
            created += seedIfAbsent(unitRepository, questionRepository, "etc", ETC_QUESTIONS);
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
