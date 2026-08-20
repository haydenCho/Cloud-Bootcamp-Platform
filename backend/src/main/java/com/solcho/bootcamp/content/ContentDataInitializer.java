package com.solcho.bootcamp.content;

import com.solcho.bootcamp.content.entity.Content;
import com.solcho.bootcamp.content.repository.ContentRepository;
import com.solcho.bootcamp.unit.entity.Unit;
import com.solcho.bootcamp.unit.repository.UnitRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

/**
 * GENERAL 단원 몇 개에 샘플 학습 본문(HTML)을 시드한다. (4단계, 관리자 에디터는 6단계)
 * 단원 시드(UnitDataInitializer, @Order(1)) 이후 실행되도록 @Order(2).
 */
@Configuration
@Order(2)
public class ContentDataInitializer {

    private static final Logger log = LoggerFactory.getLogger(ContentDataInitializer.class);

    @Bean
    CommandLineRunner seedContents(UnitRepository unitRepository, ContentRepository contentRepository) {
        return args -> {
            int created = 0;
            created += seedIfAbsent(unitRepository, contentRepository, "cloud-intro",
                    "클라우드 컴퓨팅이란?", CLOUD_INTRO_BODY);
            created += seedIfAbsent(unitRepository, contentRepository, "linux",
                    "리눅스 기초와 파일 권한", LINUX_BODY);
            if (created > 0) {
                log.info("학습 콘텐츠 시드 생성 완료: {}개", created);
            }
        };
    }

    private int seedIfAbsent(UnitRepository unitRepository, ContentRepository contentRepository,
                             String code, String title, String body) {
        Unit unit = unitRepository.findByCode(code).orElse(null);
        if (unit == null || contentRepository.existsByUnitId(unit.getId())) {
            return 0;
        }
        contentRepository.save(Content.builder()
                .unitId(unit.getId())
                .title(title)
                .body(body)
                .build());
        return 1;
    }

    private static final String CLOUD_INTRO_BODY = """
            <h2>클라우드 컴퓨팅이란?</h2>
            <p><strong>클라우드 컴퓨팅</strong>은 서버, 스토리지, 데이터베이스, 네트워크 같은
            IT 자원을 인터넷을 통해 필요할 때 빌려 쓰고, 사용한 만큼 비용을 지불하는 방식입니다.
            직접 물리 장비를 구매·운영하지 않아도 되므로 초기 비용과 운영 부담이 크게 줄어듭니다.</p>

            <h3>서비스 모델</h3>
            <ul>
              <li><strong>IaaS</strong> — 가상 서버·스토리지 등 인프라를 제공 (예: AWS EC2)</li>
              <li><strong>PaaS</strong> — 애플리케이션 실행 플랫폼을 제공</li>
              <li><strong>SaaS</strong> — 완성된 소프트웨어를 서비스로 제공 (예: Gmail)</li>
            </ul>

            <h3>배포 모델</h3>
            <p>퍼블릭 클라우드, 프라이빗 클라우드, 그리고 둘을 섞은 하이브리드 클라우드가 있습니다.
            학습 초기에는 퍼블릭 클라우드(AWS, GCP, Azure)를 기준으로 개념을 익히는 것이 좋습니다.</p>

            <h3>대표적인 CLI 예시</h3>
            <p>AWS CLI 로 S3 버킷 목록을 조회하는 명령은 다음과 같습니다.</p>
            <pre><code>aws s3 ls
            aws ec2 describe-instances --region ap-northeast-2</code></pre>

            <p>클라우드의 핵심 이점은 <em>탄력성(Elasticity)</em>입니다. 트래픽이 늘면 자원을 늘리고,
            줄면 줄여서 비용을 최적화할 수 있습니다. 이어지는 단원에서 리눅스·네트워크·도커·쿠버네티스를
            차례로 학습하며 실제 인프라를 다루는 감각을 익히게 됩니다.</p>
            """;

    private static final String LINUX_BODY = """
            <h2>리눅스 기초</h2>
            <p>리눅스는 서버 환경에서 가장 널리 쓰이는 운영체제입니다. 대부분의 클라우드 서버,
            도커 컨테이너, 쿠버네티스 노드가 리눅스 위에서 동작하므로 기본 명령어에 익숙해지는 것이 중요합니다.</p>

            <h3>기본 파일 명령어</h3>
            <pre><code>pwd            # 현재 위치
            ls -al         # 숨김 파일 포함 상세 목록
            cd /var/log    # 디렉터리 이동
            cat file.txt   # 파일 내용 출력</code></pre>

            <h3>파일 권한</h3>
            <p>리눅스의 파일 권한은 <strong>소유자(Owner) / 그룹(Group) / 그 외(Others)</strong>
            세 주체에 대해 읽기(r)·쓰기(w)·실행(x) 권한으로 구성됩니다.</p>
            <pre><code>-rwxr-xr--  1 user group  4096 Aug 20 10:00 script.sh</code></pre>
            <p>권한을 바꿀 때는 <code>chmod</code>, 소유자를 바꿀 때는 <code>chown</code> 명령을 사용합니다.</p>
            <pre><code>chmod 755 script.sh
            chown user:group script.sh</code></pre>

            <h3>프로세스와 패키지</h3>
            <p><code>ps</code>, <code>top</code> 으로 프로세스를 확인하고, 배포판에 따라
            <code>apt</code>(데비안/우분투) 또는 <code>yum</code>/<code>dnf</code>(RHEL 계열)로
            패키지를 관리합니다. 이 명령들은 이후 실습 단원에서 직접 다뤄봅니다.</p>
            """;
}
