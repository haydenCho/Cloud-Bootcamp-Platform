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
            created += seedIfAbsent(unitRepository, contentRepository, "shell",
                    "쉘 스크립트 기초와 텍스트 가공", SHELL_BODY);
            created += seedIfAbsent(unitRepository, contentRepository, "python",
                    "파이썬 기초 — 자료형과 컨테이너", PYTHON_BODY);
            created += seedIfAbsent(unitRepository, contentRepository, "aws",
                    "AWS 핵심 서비스 이해하기", AWS_BODY);
            created += seedIfAbsent(unitRepository, contentRepository, "docker",
                    "도커(Docker) 기초", DOCKER_BODY);
            created += seedIfAbsent(unitRepository, contentRepository, "network",
                    "네트워크 기초 — 계층 구조와 핵심 프로토콜", NETWORK_BODY);
            created += seedIfAbsent(unitRepository, contentRepository, "security",
                    "웹 보안 기초 — 암호화, 인증, 주요 공격과 방어", SECURITY_BODY);
            created += seedIfAbsent(unitRepository, contentRepository, "k8s",
                    "쿠버네티스(K8s) 기초 — Pod, Deployment, Service, 네임스페이스", K8S_BODY);
            created += seedIfAbsent(unitRepository, contentRepository, "etc",
                    "알아두면 좋은 보충 지식", ETC_BODY);
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

    private static final String SHELL_BODY = """
<h2>쉘 스크립트</h2>
<p>쉘 스크립트는 여러 리눅스 명령어를 파일에 모아 자동으로 실행하는 프로그램입니다. 반복 작업 자동화, 서버 점검, 로그 분석 등 실무에서 널리 쓰이며, 여기서는 Bash를 기준으로 설명합니다.</p>

<h3>스크립트 기본 구조</h3>
<p>스크립트의 첫 줄에는 <strong>Shebang</strong>을 적어 어떤 인터프리터로 실행할지 지정합니다. <code>#</code>으로 시작하는 줄은 주석으로 실행되지 않습니다. 작성 후에는 실행 권한을 준 뒤 실행합니다.</p>
<pre><code>#!/bin/bash
# 첫 번째 쉘 스크립트
echo "Hello, World!"
echo "현재 사용자: $USER"</code></pre>
<ul>
<li><code>chmod +x hello.sh</code> 로 실행 권한 부여</li>
<li><code>./hello.sh</code> 또는 <code>bash hello.sh</code> 로 실행</li>
</ul>

<h3>변수와 입출력</h3>
<p>변수는 <code>=</code> 앞뒤에 공백 없이 대입합니다. 큰따옴표 안의 <code>$변수</code>는 값으로 치환되고, 작은따옴표 안에서는 문자 그대로 남습니다. 출력은 <code>echo</code>, 형식 지정 출력은 <code>printf</code>, 입력은 <code>read</code>를 사용합니다.</p>
<pre><code>name="홍길동"
echo "환영합니다, ${name}님!"
printf "이름: %-10s 나이: %3d" "$name" 25
read -p "점수를 입력하세요: " score</code></pre>
<p><code>$1</code>, <code>$2</code>는 스크립트에 전달된 인수를, <code>$#</code>은 인수 개수, <code>$?</code>는 직전 명령의 종료 코드(0이면 성공)를 의미합니다.</p>

<h3>조건문과 반복문</h3>
<p>정수 비교는 <code>-eq</code>, <code>-lt</code>, <code>-ge</code> 같은 연산자를, 파일 검사는 <code>-f</code>(파일), <code>-d</code>(디렉토리)를 사용합니다. 정해진 횟수 반복은 <code>for</code>, 조건 기반 반복은 <code>while</code>이 알맞습니다.</p>
<pre><code>if [ $score -ge 90 ]; then
    echo "A학점"
elif [ $score -ge 80 ]; then
    echo "B학점"
else
    echo "재수강 대상"
fi

for i in {1..5}; do
    echo "번호: $i"
done</code></pre>
<p><code>while</code>과 <code>read</code>를 함께 쓰면 파일을 한 줄씩 처리할 수 있습니다. <code>IFS</code>로 구분자를 지정하면 필드를 나눠 받을 수 있습니다.</p>
<pre><code>while IFS=':' read -r user x uid rest; do
    if [ $uid -eq 0 ]; then
        echo "UID 0 계정: $user"
    fi
done < /etc/passwd</code></pre>

<h3>텍스트 가공과 파이프라인</h3>
<p>여러 명령을 파이프(<code>|</code>)로 연결해 로그를 분석하는 것이 쉘의 강점입니다. 자주 쓰는 도구는 다음과 같습니다.</p>
<ul>
<li><code>grep</code>: 패턴에 맞는 줄만 필터링 (<code>-i</code> 대소문자 무시, <code>-n</code> 줄번호, <code>-v</code> 제외)</li>
<li><code>cut</code>: 구분자로 열 잘라내기 (<code>-d</code> 구분자, <code>-f</code> 필드 번호)</li>
<li><code>sort</code> / <code>uniq</code>: 정렬과 중복 집계 (<code>uniq -c</code>는 반드시 <code>sort</code> 뒤에)</li>
<li><code>awk</code>: 필드 기반 추출·조건 처리, <code>sed</code>: 스트림 치환·삭제</li>
</ul>
<p>리다이렉트로 출력을 파일에 저장할 수도 있습니다. <code>&gt;</code>는 덮어쓰기, <code>&gt;&gt;</code>는 이어붙이기이며, <code>2&gt;&amp;1</code>은 에러 출력을 표준 출력에 합칩니다.</p>
<pre><code># 가장 많이 접속한 IP 상위 5개
awk '{print $1}' access.log | sort | uniq -c | sort -nr | head -5</code></pre>
<p>로그 분석의 정석은 <strong>필터링 → 필드 추출 → 정렬 → 빈도 집계 → 상위 N개</strong> 순서로 파이프를 이어가는 것입니다.</p>
""";

    private static final String PYTHON_BODY = """
<h2>파이썬 기초 — 자료형과 컨테이너</h2>
<p>파이썬은 문법이 간결하고 배우기 쉬운 <strong>인터프리터 언어</strong>입니다. 컴파일해서 실행 파일을 만드는 대신 해석기가 코드를 한 줄씩 읽어 실행하므로, 실행 환경에도 파이썬 해석기가 설치되어 있어야 합니다. 소스 파일은 <code>python 파일명</code> 명령으로 바로 실행할 수 있습니다.</p>

<h3>변수와 기본 자료형</h3>
<p>파이썬은 타입을 미리 지정하지 않아도 값에 따라 타입이 자동으로 정해지며, 형변환 함수로 임의 변경도 가능합니다. 대표적인 기본 자료형은 다음과 같습니다.</p>
<ul>
<li><code>int</code>: 정수 (예: 10, -5)</li>
<li><code>float</code>: 실수 (예: 3.14)</li>
<li><code>str</code>: 문자열 (예: "파이썬")</li>
<li><code>bool</code>: 참/거짓 (True, False) — <strong>int의 하위 타입</strong>이라 True는 1, False는 0으로 연산됩니다.</li>
<li><code>NoneType</code>: 값이 없음을 표현하는 None</li>
</ul>
<p>변수명은 대소문자를 구분하며, 숫자로 시작할 수 없고 예약어는 쓸 수 없습니다.</p>

<h3>연산자와 형변환</h3>
<p>나눗셈 연산자 <code>/</code>는 항상 실수(float)를 반환하고, <code>//</code>는 몫을 정수로, <code>%</code>는 나머지를, <code>**</code>는 거듭제곱을 계산합니다. 문자열은 곱셈과 문자열끼리의 덧셈만 가능합니다.</p>
<pre><code>print(10/2)     # 5.0  (항상 float)
print(10//2)    # 5    (정수 몫)
print(10%3)     # 1    (나머지)
print(2**3)     # 8    (거듭제곱)
print("a"*3)    # aaa  (문자열 반복)</code></pre>
<p>빈 값은 모두 거짓으로 취급됩니다. 예를 들어 <code>0</code>, 빈 문자열, 빈 리스트, <code>None</code>은 <code>bool()</code>로 변환하면 False가 됩니다.</p>

<h3>컨테이너 자료형</h3>
<p>여러 값을 담는 컨테이너 자료형은 다음 네 가지가 핵심입니다.</p>
<ul>
<li><strong>list</strong>: 대괄호로 생성, 인덱스 0부터 시작, 값 변경이 자유로운 <em>mutable</em> 자료형</li>
<li><strong>tuple</strong>: 소괄호로 생성, 값 변경 불가한 <em>immutable</em> 읽기 전용 자료형 (요소 1개면 쉼표 필수)</li>
<li><strong>dict</strong>: 중괄호에 키와 값 형태로 저장, 키는 유일하며 삽입 순서를 유지</li>
<li><strong>set</strong>: 중괄호로 생성하되 빈 집합은 set(), 중복을 자동 제거하고 순서가 없음</li>
</ul>
<pre><code>fooList = [1, 2, 3]
fooList.append(4)          # 끝에 추가
empDict = {"이름": "홍길동"}
print("이름" in empDict)   # True  (키 존재 확인)
nums = set([1, 1, 2])      # {1, 2}  중복 제거</code></pre>

<h3>컴프리헨션과 입출력</h3>
<p>컴프리헨션을 쓰면 반복문을 한 줄로 간결하게 표현할 수 있습니다. <code>input()</code>은 입력값을 항상 문자열로 받으므로 숫자가 필요하면 형변환 함수와 함께 씁니다.</p>
<pre><code>squares = [i**2 for i in range(1, 6)]   # [1, 4, 9, 16, 25]
evens = [x for x in range(10) if x%2==0]  # [0, 2, 4, 6, 8]

age = int(input("나이: "))    # 문자열을 정수로 변환
name = "홍길동"
print(f"이름: {name}, 나이: {age}")   # f-string</code></pre>
""";

    private static final String AWS_BODY = """
<h2>AWS 핵심 서비스 이해하기</h2>
<p>AWS는 아마존이 제공하는 클라우드 컴퓨팅 플랫폼으로, 서버 구매 없이 필요한 만큼 자원을 빌려 쓰고 사용한 만큼만 비용을 내는 <strong>종량제</strong> 방식으로 동작합니다. 실습에서는 EC2로 서버를 띄우고, 보안 그룹으로 접근을 열고, RDS와 로드 밸런서까지 연결하며 하나의 서비스를 완성했습니다.</p>
<h3>리전과 가용 영역</h3>
<p>AWS 자원은 전 세계에 흩어진 <strong>리전(Region)</strong>에 배치됩니다. 서울 리전은 <code>ap-northeast-2</code>처럼 코드로 표현합니다.</p>
<ul>
<li>리전: 지리적으로 떨어진 데이터센터 집합으로, 사용자와 가까운 리전을 고르면 지연이 줄어듭니다.</li>
<li>가용 영역(AZ): 한 리전 안의 물리적으로 분리된 데이터센터로, 여러 AZ에 나눠 배치하면 장애에 강해집니다.</li>
</ul>
<h3>EC2와 보안 그룹</h3>
<p><strong>EC2</strong>는 가상 서버(인스턴스)를 빌려주는 컴퓨팅 서비스입니다. AMI로 운영체제를 고르고, 인스턴스 타입으로 성능을 정하며, 키 페어로 접속을 인증합니다. 인스턴스로 들어오는 트래픽은 <em>보안 그룹</em>이라는 가상 방화벽이 포트 단위로 허용하거나 차단합니다.</p>
<pre><code>aws ec2 describe-instances --region ap-northeast-2</code></pre>
<h3>S3와 IAM</h3>
<p><strong>S3</strong>는 파일을 <em>버킷</em>이라는 공간에 객체 단위로 저장하는 스토리지입니다. 이미지, 백업, 정적 웹 파일 저장에 널리 쓰입니다. 이런 자원에 누가 접근할지는 <strong>IAM</strong>이 사용자, 그룹, 역할과 정책으로 관리합니다. EC2에 역할을 붙이면 키를 코드에 넣지 않고도 다른 서비스를 안전하게 호출할 수 있습니다.</p>
<pre><code>aws s3 ls
aws s3 cp report.txt s3://my-bucket/</code></pre>
<h3>VPC와 서비스 확장</h3>
<p><strong>VPC</strong>는 내 계정만의 격리된 가상 네트워크로, 서브넷과 라우팅으로 자원을 배치하는 뼈대입니다. 트래픽이 늘면 로드 밸런서(ELB)로 여러 인스턴스에 요청을 분산하고, 오토 스케일링으로 인스턴스 수를 자동으로 늘리거나 줄입니다. 데이터베이스는 관리형 서비스인 RDS로 백업과 장애 조치를 맡기면 운영 부담이 줄어듭니다.</p>
""";

    private static final String DOCKER_BODY = """
<h2>도커(Docker) 기초</h2>
<p><strong>도커</strong>는 애플리케이션을 실행 환경까지 통째로 묶어 <em>컨테이너</em>라는 격리된 공간에서 돌리는 도구입니다. 가상머신(VM)이 게스트 OS를 통째로 올리는 것과 달리, 컨테이너는 호스트 OS의 리소스를 논리적으로 나눠 공유하므로 가볍고 빠릅니다.</p>
<h3>이미지와 컨테이너</h3>
<p><strong>이미지</strong>는 한 번 만들어지면 변하지 않는 읽기 전용 템플릿(레시피)이고, <strong>컨테이너</strong>는 그 이미지를 실행해 실제로 돌아가는 인스턴스(요리)입니다. 하나의 이미지로 여러 컨테이너를 동시에 띄울 수 있고, 각 컨테이너는 서로 격리되어 하나가 망가져도 다른 것에 영향을 주지 않습니다.</p>
<ul>
<li><code>docker run</code>: 이미지 다운로드 + 생성 + 실행을 한 번에 수행</li>
<li><code>docker stop</code> / <code>docker rm</code>: 컨테이너 정지 / 삭제</li>
<li><code>-d</code> 옵션: 컨테이너를 백그라운드(detach)로 실행</li>
</ul>
<pre><code>docker run -d -p 8080:80 nginx
docker ps
docker stop web</code></pre>
<h3>포트와 볼륨</h3>
<p>컨테이너는 기본적으로 외부와 격리되어 있어, 브라우저에서 접속하려면 포트를 열어야 합니다. <code>-p 8080:80</code>은 호스트의 8080 포트를 컨테이너의 80 포트로 연결하는 문입니다. 또한 컨테이너를 삭제하면 안의 데이터도 사라지므로, DB 데이터처럼 살아남아야 하는 것은 <strong>볼륨</strong>에 저장해 컨테이너 수명과 데이터 수명을 분리합니다.</p>
<pre><code>docker run -v mydata:/var/lib/mysql mariadb</code></pre>
<h3>Dockerfile로 이미지 만들기</h3>
<p>남의 이미지를 가져다 쓰는 것을 넘어, <strong>Dockerfile</strong>(이미지를 만드는 레시피)을 작성하고 <code>docker build</code>로 내 이미지를 만들 수 있습니다. 자주 바뀌지 않는 명령을 위쪽에 두면 <em>빌드 캐시</em>가 재사용되어 빌드가 빨라집니다.</p>
<ul>
<li><strong>FROM</strong>: 베이스 이미지 지정 (첫 줄)</li>
<li><strong>COPY</strong> / <strong>RUN</strong>: 파일 복사 / 빌드 시점 명령 실행</li>
<li><strong>CMD</strong>: 컨테이너 시작 시 실행할 기본 명령</li>
</ul>
<pre><code>docker build -t myapp:1.0 .
docker run -d -p 8080:3000 myapp:1.0</code></pre>
<h3>Docker Compose</h3>
<p>여러 컨테이너를 하나씩 실행하고 IP를 손으로 맞추는 대신, <code>docker-compose.yml</code> 파일 하나로 정의하고 <code>docker compose up -d</code> 한 줄로 전부 띄웁니다. 같은 파일 안의 서비스끼리는 자동으로 같은 네트워크에 묶여, IP 대신 <strong>서비스 이름</strong>으로 서로를 부를 수 있습니다.</p>
<pre><code>docker compose up -d
docker compose down</code></pre>
""";

    private static final String NETWORK_BODY = """
<h2>네트워크 기초</h2>
<p>네트워크는 여러 컴퓨터가 규칙(프로토콜)에 따라 데이터를 주고받는 구조입니다. 복잡한 통신 과정을 이해하기 쉽게 나눈 것이 <strong>계층 모델</strong>이며, 이론용 참조 모델인 <strong>OSI 7계층</strong>과 실제 구현 구조인 <strong>TCP/IP 4계층</strong>이 대표적입니다.</p>

<h3>계층 구조</h3>
<ul>
<li><strong>물리 계층</strong>: 전기 신호로 장치를 연결합니다(허브, 리피터).</li>
<li><strong>데이터 링크 계층</strong>: 같은 네트워크 안에서 <strong>MAC 주소</strong>로 통신합니다(스위치).</li>
<li><strong>네트워크 계층</strong>: <strong>IP</strong> 주소로 다른 네트워크까지 경로를 찾습니다(라우터).</li>
<li><strong>전송 계층</strong>: <strong>TCP</strong>와 <strong>UDP</strong>로 양 끝단 사이의 데이터 전달을 담당합니다.</li>
</ul>
<p>MAC 주소는 구간마다 교체되지만, IP 주소는 최종 목적지까지 그대로 유지된다는 점이 핵심입니다.</p>

<h3>IP 주소와 서브넷</h3>
<p>IP 주소는 네트워크 부분과 호스트 부분으로 나뉘며, 어디까지가 네트워크인지 알려 주는 것이 <strong>서브넷 마스크</strong>입니다. 예를 들어 <code>/24</code>는 <code>255.255.255.0</code>을 뜻하고, IP와 서브넷 마스크를 <em>AND 연산</em>하면 그 주소가 속한 네트워크 주소를 구할 수 있습니다.</p>
<pre><code>ip addr show
ping 8.8.8.8</code></pre>

<h3>포트와 주요 프로토콜</h3>
<p>포트 번호는 16비트(0~65535)로, 도착한 패킷이 찾아갈 응용 프로그램의 통로입니다. 0~1023번은 <strong>잘 알려진 포트</strong>로 주요 서비스에 배정되어 있습니다.</p>
<ul>
<li><strong>DNS</strong>(53): 도메인 이름을 IP 주소로 변환하며 주로 UDP를 사용합니다.</li>
<li><strong>HTTP</strong>(80): 웹 페이지를 주고받는 기본 프로토콜입니다.</li>
<li><strong>SSH</strong>(22), <strong>HTTPS</strong>(443): 원격 접속과 암호화된 웹 통신에 사용됩니다.</li>
</ul>
<pre><code>curl http://example.com
nslookup naver.com</code></pre>
<p>TCP는 3-way handshake(SYN, SYN+ACK, ACK)로 연결을 맺고 재전송으로 신뢰성을 보장하는 반면, UDP는 연결 과정 없이 빠르고 가볍게 전송합니다.</p>
""";

    private static final String SECURITY_BODY = """
<h2>웹 보안 기초 — 암호화, 인증, 주요 공격과 방어</h2>
<p>보안은 데이터를 안전하게 지키고, 올바른 사용자만 자원에 접근하도록 통제하는 기술입니다. 이 단원에서는 암호화와 해시, 인증과 인가의 차이, 대표적인 웹 공격과 그 방어법을 정리합니다.</p>

<h3>암호화와 해시</h3>
<p>암호화는 데이터를 알아볼 수 없게 바꾸는 기술이며, 크게 두 종류로 나뉩니다.</p>
<ul>
<li><strong>양방향 암호화</strong>: 암호화한 값을 다시 원래 데이터로 되돌리는 <em>복호화</em>가 가능합니다. 예로 TLS 통신 암호화가 있습니다.</li>
<li><strong>단방향 암호화(해시)</strong>: 데이터를 고정된 길이의 문자열로 바꾸며 복호화가 불가능합니다. <code>MD5</code>, <code>SHA-256</code>, <code>SHA-512</code> 등이 있습니다.</li>
</ul>
<p>비밀번호는 유출되어도 원문을 알 수 없도록 반드시 해시로 저장합니다. 특히 <strong>BCrypt</strong>는 의도적으로 계산을 느리게 만들어 무차별 대입 공격을 어렵게 하므로 비밀번호 저장에 권장됩니다.</p>

<h3>인증과 인가</h3>
<p>두 개념은 자주 혼동되지만 역할이 다릅니다.</p>
<ul>
<li><strong>인증(Authentication)</strong>: "당신이 누구인가"를 확인하는 절차입니다. 로그인 시 아이디와 비밀번호를 검증하는 단계가 여기에 해당합니다.</li>
<li><strong>인가(Authorization)</strong>: "이 자원에 접근할 권한이 있는가"를 확인하는 절차입니다. 일반 사용자와 관리자의 권한을 구분하는 것이 예입니다.</li>
</ul>
<p>웹에서는 로그인 후 서버가 세션 ID나 토큰을 발급해 로그인 상태를 유지합니다. 이때 세션 식별자가 탈취되면 남의 계정을 그대로 사용할 수 있으므로 안전하게 관리해야 합니다.</p>

<h3>주요 공격과 근본 방어</h3>
<p>대표적인 웹 위협과 대응은 다음과 같습니다.</p>
<ul>
<li><strong>SQL 인젝션</strong>: 입력값에 악의적인 SQL을 끼워 넣어 DB를 조작하는 공격. 파라미터 바인딩과 입력 검증으로 막습니다.</li>
<li><strong>XSS(Cross-Site Scripting)</strong>: 게시글 등에 스크립트를 심어 다른 사용자 브라우저에서 실행시키는 공격. 저장 전 HTML을 <em>sanitize</em>하여 막습니다.</li>
<li><strong>패킷 스니핑</strong>: 네트워크를 지나는 평문 데이터를 몰래 훔쳐보는 도청. HTTP는 평문이라 아이디와 비밀번호가 그대로 노출됩니다.</li>
</ul>
<p>스니핑의 근본 방어는 통신을 암호화하는 <strong>HTTPS/TLS</strong>입니다. 여기에 <code>Strict-Transport-Security</code>(HSTS) 헤더를 더하면 항상 암호화된 연결만 쓰도록 강제할 수 있습니다.</p>

<h3>방화벽과 IDS/IPS</h3>
<p>네트워크 경계에서는 방화벽과 탐지 시스템으로 트래픽을 통제합니다.</p>
<ul>
<li><strong>방화벽</strong>: 인바운드 트래픽을 규칙에 따라 허용하거나 차단합니다. 등록된 IP만 허용하는 <strong>화이트리스트</strong> 방식은 무차별 대입 공격을 줄여 줍니다.</li>
<li><strong>IDS</strong>: 침입을 탐지해 관리자에게 경고만 보냅니다. 보안 카메라에 비유됩니다.</li>
<li><strong>IPS</strong>: 탐지에 더해 악성 트래픽을 실시간으로 차단합니다. 경비원에 비유됩니다.</li>
</ul>
<p>방어는 특정 계층 하나가 아니라 계층별로 빠짐없이 적용해야 효과가 있습니다.</p>
""";

    private static final String K8S_BODY = """
<h2>쿠버네티스(K8s) 기초</h2>
<p><strong>쿠버네티스</strong>는 여러 대의 서버(노드)를 하나의 클러스터로 묶어 컨테이너를 자동으로 배포하고 관리해 주는 오케스트레이션 도구입니다. 사용자는 <code>kubectl</code> 이라는 명령줄 도구로 클러스터에 원하는 상태를 선언하고, 쿠버네티스가 그 상태를 계속 맞춰 줍니다.</p>

<h3>핵심 오브젝트</h3>
<p>쿠버네티스에서 가장 자주 다루는 오브젝트는 다음과 같습니다.</p>
<ul>
<li><strong>Pod</strong>: 컨테이너를 실행하는 가장 작은 배포 단위입니다. 하나의 Pod 안에 한 개 이상의 컨테이너가 함께 묶여 같은 네트워크와 저장소를 공유합니다.</li>
<li><strong>ReplicaSet</strong>: 지정한 개수만큼 Pod 복제본이 항상 유지되도록 보장합니다. Pod가 죽으면 자동으로 다시 만듭니다.</li>
<li><strong>Deployment</strong>: ReplicaSet을 관리하며 배포, 롤백, 스케일링을 담당합니다. 실무에서 애플리케이션을 올릴 때 보통 이 오브젝트를 사용합니다.</li>
<li><strong>Service</strong>: 여러 Pod에 하나의 고정된 접근 지점(고정 IP와 이름)을 제공하고 트래픽을 <em>로드밸런싱</em> 합니다.</li>
</ul>

<h3>kubectl 기본 명령어</h3>
<p>클러스터를 조작하는 가장 기본적인 명령어들입니다. <code>apply</code> 는 YAML 파일에 선언한 상태를 적용하며 멱등성을 보장합니다.</p>
<pre><code>kubectl get pods -A
kubectl describe pod mypod
kubectl apply -f deploy.yaml
kubectl scale deployment web --replicas=3
kubectl logs -f mypod</code></pre>

<h3>YAML 매니페스트 예시</h3>
<p>원하는 상태를 <strong>YAML 매니페스트</strong>로 선언하고 <code>apply</code> 로 적용합니다. 아래는 Pod 3개를 유지하는 Deployment 예시입니다.</p>
<pre><code>apiVersion: apps/v1
kind: Deployment
metadata:
  name: web
spec:
  replicas: 3
  selector:
    matchLabels:
      app: web
  template:
    metadata:
      labels:
        app: web
    spec:
      containers:
      - name: nginx
        image: nginx:1.14</code></pre>

<h3>네임스페이스</h3>
<p><strong>Namespace</strong>는 하나의 물리 클러스터를 여러 개의 논리적 가상 클러스터로 나누는 리소스입니다. 팀별, 환경별(dev, staging, production)로 리소스를 격리할 때 사용합니다.</p>
<ul>
<li><code>default</code>: 네임스페이스를 지정하지 않으면 기본으로 할당됩니다.</li>
<li><code>kube-system</code>: 쿠버네티스 시스템 오브젝트가 위치하는 관리자 영역입니다.</li>
<li>같은 이름의 리소스라도 서로 다른 네임스페이스에서는 동시에 존재할 수 있습니다.</li>
</ul>
<pre><code>kubectl create namespace blue
kubectl get pods -n blue
kubectl config set-context --current --namespace=blue</code></pre>
""";

    private static final String ETC_BODY = """
<h2>알아두면 좋은 보충 지식</h2>
<p>이 단원은 특정 주제에 딱 들어맞지는 않지만 부트캠프 과정에서 자주 마주치는 잡다한 보충 지식을 가볍게 모아둔 공간입니다. 필요할 때 참고하고, 더 깊은 내용은 각 전용 단원에서 다룹니다.</p>

<h3>버전 관리: Git</h3>
<p><strong>Git</strong>은 코드 변경 이력을 관리하는 분산 버전 관리 시스템입니다. 여러 사람이 같은 코드를 안전하게 나눠 작업하고 합칠 수 있게 해줍니다. 기본 흐름은 작업 → 스테이징(add) → 커밋(commit) → 푸시(push)입니다.</p>
<pre><code>git status
git add .
git commit -m "기능 추가"
git push origin main</code></pre>

<h3>설정 파일 포맷: JSON 과 YAML</h3>
<p>데이터를 사람이 읽기 쉽게 표현하는 대표적인 포맷입니다. JSON 은 중괄호와 따옴표로, YAML 은 들여쓰기로 구조를 표현합니다.</p>
<ul>
  <li><strong>JSON</strong> — API 응답, package.json 등에 널리 쓰입니다.</li>
  <li><strong>YAML</strong> — docker-compose.yml, 쿠버네티스 매니페스트처럼 설정 파일에 자주 쓰입니다.</li>
</ul>

<h3>환경 변수와 .env</h3>
<p>비밀번호, API 키처럼 민감하거나 환경마다 달라지는 값은 코드에 직접 쓰지 않고 <strong>환경 변수</strong>나 <code>.env</code> 파일로 분리해 관리합니다. <code>.env</code> 는 보통 버전 관리에서 제외(<code>.gitignore</code>)해 GitHub 에 올라가지 않게 합니다.</p>

<h3>CI/CD</h3>
<p><strong>CI</strong>(지속적 통합)는 코드를 자주 병합하면서 자동으로 빌드·테스트해 문제를 빨리 발견하는 것을, <strong>CD</strong>(지속적 배포)는 검증된 코드를 자동으로 배포하는 것을 뜻합니다. 대표 도구로 GitHub Actions, Jenkins 등이 있습니다.</p>
""";
}
