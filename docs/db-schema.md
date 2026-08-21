# DB 설계 초안 — 클라우드 부트캠프 학습 플랫폼

이 문서는 MariaDB 기준 테이블 설계 초안입니다. 컬럼명·타입은 개발하면서 조정될 수 있습니다. 1인 개발 프로젝트 규모에 맞춰 과도한 정규화나 캐시 테이블은 피하고, 계산 가능한 값(진도율 등)은 저장하지 않고 조회 시 계산하는 방향으로 설계했습니다.

## 설계 원칙

프로필 사진/닉네임은 게시글·댓글에 복사해서 저장하지 않습니다. `user_id` 외래키로만 연결하고 조회 시 조인하면, 프로필을 바꾸는 즉시 과거 글에도 자동으로 반영되어 별도 동기화 로직이 필요 없습니다. 실습 미션 완료 여부나 빈칸 정답 여부처럼 항목 단위 데이터는 항목 테이블에 직접 저장하고, "이 단원을 완료했는가"는 저장하지 않고 하위 항목을 집계해서 판단합니다(예: 해당 unit의 모든 blank_question이 정답이면 완료). 서비스 좋아요처럼 사용자당 값이 하나뿐인 것은 별도 테이블 대신 `user` 테이블 컬럼으로 둬도 되지만, 여기서는 나중에 좋아요 취소/통계를 더 유연하게 다루기 쉽도록 별도 테이블로 분리했습니다. 프로젝트 규모가 계속 작다면 컬럼으로 합쳐도 무방합니다.

## 테이블

### user

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK AUTO_INCREMENT | |
| login_id | VARCHAR(50) UNIQUE | 로그인 아이디 (예: admin, guest) |
| password_hash | VARCHAR(255) | BCrypt 해시 |
| nickname | VARCHAR(50) | |
| profile_image_url | VARCHAR(255) NULL | |
| role | ENUM('USER','ADMIN') DEFAULT 'USER' | |
| refresh_token_hash | VARCHAR(255) NULL | 현재 유효한 refresh token의 해시 (로그아웃 시 NULL 처리) |
| refresh_token_expires_at | DATETIME NULL | |
| created_at | DATETIME | |
| updated_at | DATETIME | |

초기 데이터: `admin`(role=ADMIN), `guest`(role=USER, 시연용) 시드.

### unit (학습 단원)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| code | VARCHAR(50) UNIQUE | 슬러그 (예: `linux`, `linux-practice`) |
| name | VARCHAR(100) | 화면 표시명 (예: "리눅스") |
| group_code | VARCHAR(50) | 로드맵상 같은 그룹으로 묶기 위한 키 (예: `linux` → 리눅스/리눅스(실습) 공통) |
| type | ENUM('GENERAL','PRACTICE') | 일반 학습 단원인지 실습 단원인지 |
| icon_image_path | VARCHAR(255) | `frontend/public/assets/imgs/roadmap/` 하위 경로 |
| sort_order | INT | 로드맵 표시 순서 |
| created_at | DATETIME | |

### content (학습 본문 묶음)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| unit_id | BIGINT FK → unit.id | 단원에 연결(GENERAL·PRACTICE 공통) |
| title | VARCHAR(200) | 단원 학습 본문의 제목 |
| created_at / updated_at | DATETIME | |

> **8단계 개선**: 기존 `body`(통짜 HTML) 컬럼을 제거하고, 실제 본문은 `content_chapter`(챕터)로 분리했습니다.
> `content` 는 단원당 1행으로 제목만 갖고, 챕터들이 이 content 를 참조합니다.

### content_chapter (학습 본문 챕터)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| content_id | BIGINT FK → content.id | |
| title | VARCHAR(300) | 챕터 제목(원본 HTML 의 섹션 헤딩 텍스트) |
| body | LONGTEXT | 챕터 본문 HTML (렌더링 전 sanitize) |
| sort_order | INT | 챕터 순서(1부터) |
| created_at / updated_at | DATETIME | |

`DataInitializer`(`ContentDataInitializer`)가 `resources/content/{code}.html` 파일을 기동 시 파싱해 챕터로 만듭니다.
분리 기준은 "파일에서 실제 섹션을 구분하는 최상위 헤딩 레벨"입니다: 파일에 `<h2>`가 2개 이상이면 h2 로,
아니면(대부분의 이론 단원처럼 `<h2>`는 제목 1개뿐이고 섹션이 `<h3>`인 경우) `<h3>`로 나눕니다.
각 챕터 본문은 헤딩을 h2 기준으로 승격해(예: h3→h2, h4→h3) `h2(챕터 제목)+h3(하위 섹션)`으로 통일합니다.
파일 내용이 바뀌면(제목/본문/개수 비교) 해당 content 의 챕터를 전부 지우고 다시 만듭니다(idempotent).

### blank_question (빈칸 채우기 문제)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| unit_id | BIGINT FK → unit.id | |
| sentence_template | TEXT | 빈칸 위치를 포함한 문장 (예: "Linux에서 파일 권한을 변경하려면 {blank} 명령어를 사용한다.") |
| answer | VARCHAR(200) | 정답 |
| score | INT DEFAULT 10 | |
| sort_order | INT | |

관리자 에디터는 "문장 / 정답 / 점수" 3개 입력값만 받고, `{blank}` 위치를 프론트에서 `<input>`으로 치환해 렌더링하는 방식을 권장합니다. HTML을 직접 작성하지 않아도 되므로 콘텐츠 등록이 훨씬 빨라집니다.

### practice_mission (실습 미션)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| unit_id | BIGINT FK → unit.id | type='PRACTICE' 단원에 연결 |
| title | VARCHAR(200) | |
| description | TEXT | 미션 설명 |
| mission_type | ENUM('PYTHON','DB','DOCKER','K8S','SHELL') | |
| verify_pattern | VARCHAR(500) | 정답 검증용 정규식/키워드 (예: `replicas:\s*5`) |
| xp_reward | INT DEFAULT 100 | |
| sort_order | INT | |

### chapter_progress (챕터 열람 진도)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| user_id | BIGINT FK → user.id | |
| chapter_id | BIGINT FK → content_chapter.id | |
| visited_at | DATETIME | 최초 방문 시각 |

UNIQUE(user_id, chapter_id)

> **8단계 개선**: 스크롤 기반 `progress` 테이블을 제거하고 `chapter_progress` 로 대체했습니다.
> 사용자가 챕터를 열람하면 1행이 생기고(중복 방문은 무시), 단원 진도는 저장된 퍼센트 없이
> "방문한 챕터 수 / 전체 챕터 수 × 100" 으로 조회 시점에 계산합니다.
> 챕터 신규 방문 시 `activity_log`(잔디)도 함께 갱신합니다.

### blank_answer (사용자별 빈칸 답안 상태)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| user_id | BIGINT FK → user.id | |
| blank_question_id | BIGINT FK → blank_question.id | |
| user_answer | VARCHAR(200) | 마지막으로 입력한 값 (페이지 이탈 후에도 유지되어야 하므로 저장) |
| is_correct | BOOLEAN DEFAULT FALSE | |
| attempts | INT DEFAULT 0 | |
| updated_at | DATETIME | |

UNIQUE(user_id, blank_question_id). 해당 unit의 모든 blank_question에 대해 is_correct=TRUE인 행이 있으면 그 단원의 빈칸 채우기 학습이 완료된 것으로 판단합니다(별도 완료 플래그 불필요).

### mission_progress (실습 미션 완료 여부)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| user_id | BIGINT FK → user.id | |
| mission_id | BIGINT FK → practice_mission.id | |
| completed | BOOLEAN DEFAULT FALSE | |
| completed_at | DATETIME NULL | |

UNIQUE(user_id, mission_id)

### activity_log (잔디심기)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| user_id | BIGINT FK → user.id | |
| activity_date | DATE | |
| activity_count | INT DEFAULT 0 | 그날 발생한 학습 활동(진도 갱신, 빈칸 정답, 미션 완료 등) 횟수 |

UNIQUE(user_id, activity_date). progress/blank_answer/mission_progress 갱신 시 해당 날짜 행을 upsert하면 됩니다.

### community_post (커뮤니티 질문 게시글)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| user_id | BIGINT FK → user.id | 작성자 |
| title | VARCHAR(200) | |
| body | TEXT | |
| view_count | INT DEFAULT 0 | |
| created_at / updated_at | DATETIME | |

### community_comment (댓글 / 답글)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| id | BIGINT PK | |
| post_id | BIGINT FK → community_post.id | |
| user_id | BIGINT FK → user.id | |
| parent_comment_id | BIGINT FK → community_comment.id NULL | NULL이면 최상위 댓글, 값이 있으면 그 댓글에 대한 답글 |
| body | TEXT | |
| created_at / updated_at | DATETIME | |

### service_like (서비스 좋아요)

| 컬럼 | 타입 | 설명 |
|---|---|---|
| user_id | BIGINT PK, FK → user.id | 사용자당 1행 (좋아요 누른 사용자만 존재) |
| created_at | DATETIME | |

좋아요 총 개수는 `SELECT COUNT(*) FROM service_like`로 계산합니다. 취소 시 행을 삭제합니다.

## 관계 요약

```
user 1─N chapter_progress, blank_answer, mission_progress, activity_log, community_post, community_comment, service_like
unit 1─N content, blank_question, practice_mission
content 1─N content_chapter
content_chapter 1─N chapter_progress
community_post 1─N community_comment
community_comment 1─N community_comment (self, parent_comment_id로 답글)
```

## 구현 메모 (4단계 반영)

- `content` / `blank_question` 은 관리자 에디터(6단계) 전까지 `DataInitializer` 로 시드합니다. 4단계에서는 GENERAL 단원 중 클라우드 개론·리눅스에 본문을, 리눅스·쉘 스크립트에 빈칸 문제를 넣었습니다.
- `content` 는 4단계 기준 단원당 1개만 시드하며, 조회는 해당 단원의 가장 먼저 등록된 본문을 반환합니다(스키마상 1:N 은 유지).
- `progress.scroll_percent` 는 뒤로 가지 않도록 갱신 시 **기존값과의 최댓값**을 유지하고, 90 이상이면 `completed=true` + `completed_at` 을 기록합니다.
- `blank_answer` 정답 비교는 앞뒤 공백/대소문자를 무시합니다. 재제출 시 `attempts` 를 증가시킵니다. 빈칸 목록 조회 API 는 치팅 방지를 위해 정답(`answer`)을 내려주지 않고, 채점 응답에서만 정답을 노출합니다.
- 엔티티는 FK를 JPA 연관관계 대신 `*_id` 컬럼(Long)으로 단순 매핑했습니다(1인 개발 규모에 맞춘 단순화).

## 구현 메모 (8단계 반영 — 학습 콘텐츠 챕터 분리 + 진도 방식 변경)

- **content.body 제거 → content_chapter 신설**: 통짜 HTML 을 챕터로 나눠 블로그 글처럼 보여준다. `content` 는 단원당 1행(제목만), 챕터가 본문을 갖는다.
- **분리 로직(ContentDataInitializer)**: 파일을 잘라 새로 만들지 않고, 기존 `resources/content/*.html` 을 기동 시 파싱한다. 최상위 섹션 헤딩 레벨을 자동 판별(h2 ≥ 2개면 h2, 아니면 h3)해 나눈다. **지시는 "h2 기준"이었으나 이론(GENERAL) 파일 대부분이 h2 는 제목 1개뿐이고 섹션이 h3 라서, h2 로만 나누면 단원당 챕터 1개가 되어 분리 의미가 없다.** 그래서 h2 가 여러 개인 파일은 h2 로(지시대로), 그렇지 않은 파일은 h3 로 나누는 적응형 방식으로 구현했다(사용자 검토 요망). idempotent 갱신(파일↔DB 챕터 diff 후 다르면 재생성)은 유지.
- **progress(스크롤) 제거 → chapter_progress**: 진도 = 방문 챕터/전체 챕터. `GET /api/v1/progress` 의 `generalPercent` 계산만 바뀌고 응답 shape(대시보드 소비)은 그대로 유지했다. 스크롤 저장 API(`PATCH /units/{code}/progress`)와 본문 조회 API(`GET /units/{code}/content`)는 제거했다.
- **⚠️ 스키마 마이그레이션**: `spring.jpa.hibernate.ddl-auto=update` 는 컬럼/테이블을 **드롭하지 않는다**. 따라서 기존 DB 에는 `content.body`(NOT NULL) 컬럼과 `progress` 테이블이 잔존해, `body` 없이 INSERT 하면 실패한다. 개발/배포(포트폴리오) 규모라 **DB 볼륨을 새로 만들어(fresh)** 엔티티 기준으로 재생성하는 것으로 처리한다(초기 시드가 모두 재수행됨). 운영에서 데이터 보존이 필요하면 수동 `ALTER TABLE content DROP COLUMN body; DROP TABLE progress;` 후 챕터 재시드.

## 구현 메모 (8단계 반영 — 실습 단원 노트 + 이미지)

- **7개 PRACTICE 단원에 `content` 시드**: `ContentDataInitializer` SEEDS 에 `linux-practice`·`shell-practice`·`server-build-practice`·`python-practice`·`database-practice`·`docker-practice`·`k8s-practice` 를 추가했습니다. `content` 테이블은 GENERAL 전용 제약이 없어 PRACTICE `unit_id` 에도 그대로 연결됩니다(스키마 변경 없음). 조회는 기존 `GET /api/v1/units/{code}/content` 를 그대로 사용합니다.
- 본문 HTML 은 GENERAL 과 동일하게 `backend/src/main/resources/content/{code}.html` 파일 기반입니다. `linux-practice`·`shell-practice` 는 기존 파일을 재사용하고, 나머지 5개는 `notes/`의 노션 노트를 markdown→HTML 로 변환해 새로 생성했습니다.
- **K8s 실습 주의**: 전용 실습 노트가 없어 GENERAL `k8s` 와 동일한 "쿠버네티스 실무"(`notes/11.k8s`) 노트를 `k8s-practice` 에도 사용했습니다(내용 중복). 별도 K8s 실습 노트가 확보되면 교체 대상 — 사용자 검토 필요.
- **이미지**: GENERAL 7개(cloud-intro·linux·shell·python·docker·security·k8s)와 PRACTICE 7개 노트의 이미지를 `frontend/public/assets/imgs/notes/{code}/` 로 복사하고 본문 `<img src>` 를 이 웹 경로로 연결(총 318장). AWS·네트워크 GENERAL 은 노트 이미지가 각각 214·75장으로 과다해 이번엔 반영하지 않았습니다(사용자 검토 대상). `.gitignore` 의 `notes/` 패턴이 이 public 자산까지 무시하던 문제를 루트 앵커(`/notes/`)로 수정했습니다.
- **개인정보 검토 필요**: 서버 구축·데이터베이스 실습 노트 스크린샷에는 사설 IP·계정명·세션 정보 등이 노출될 수 있습니다. 지시대로 임의로 가리지 않고 원본을 유지했으니, 공개 배포 전 사용자 검토가 필요합니다.

## 구현 메모 (7단계 4/4 반영 — GENERAL 학습 콘텐츠 전체 채우기, 이후 보정)

- GENERAL 타입 10개 단원 **전부**에 대해 `content`(학습 본문)와 `blank_question`(빈칸 문제)을 시드 완료했습니다. 관리자 에디터 전까지 `ContentDataInitializer` / `BlankDataInitializer` 가 담당합니다.
- **콘텐츠 보정(7단계 4/4 직후)**: 최초 채우기 때 "기존 클라우드 개론·리눅스와 비슷한 분량으로 맞추라"는 잘못된 기준 때문에, 4단계에서 파이프라인 검증용으로 일부러 짧게 넣은 두 샘플(클라우드 개론·리눅스)의 분량에 맞춰 나머지 8개 단원까지 실제 노트보다 훨씬 짧게 요약되는 문제가 있었습니다. `notes/`의 노션 노트를 다시 참조해 클라우드 개론·리눅스를 포함한 9개 단원(그외 제외) 본문을 노트 분량에 맞게 다시 작성했습니다.
- 본문 저장 방식을 **classpath 리소스 파일 기반**으로 바꿨습니다. `backend/src/main/resources/content/{unit-code}.html` 파일에 HTML 본문을 두고, `ContentDataInitializer`가 이를 읽어 시드합니다. 노트 원문을 그대로 옮기면 단원 하나의 분량이 Java 문자열 상수(text block)의 UTF-8 65535바이트 제한을 넘을 수 있어, Java 소스에 인라인 문자열로 두는 방식(4단계~7단계 4/4)을 버리고 파일로 분리했습니다.
- **갱신 로직**: `ContentDataInitializer`는 단원에 본문이 없으면 새로 만들고, 있으면 파일 내용과 DB의 기존 `body`/`title`을 **문자열로 비교해서 다를 때만** `Content.updateBody()`로 교체합니다. 별도의 버전/체크섬 컬럼 없이 동작하며, 최초 실행 시에는 위 보정으로 짧은 본문을 새 본문으로 교체하고, 이후에는 파일과 DB 내용이 같아지므로 재기동해도 불필요한 재시딩 없이 그대로 유지됩니다(idempotent).
- 콘텐츠 출처: "그외"를 제외한 9개 단원(클라우드 개론·리눅스·쉘 스크립트·파이썬·AWS·도커·네트워크·보안·K8s) 모두 `notes/`의 노션 노트 기반. **"그외"는 대응 노트가 없어 일반 지식(Git/JSON·YAML/.env/CI·CD)으로 가볍게 작성한 상태를 유지 → 필요 시 추후 사용자 검토**.
- 빈칸 문제(`blank_question`)는 이번 보정에서 건드리지 않았습니다(단원당 4개 유지, 리눅스만 5개). 노트 분량이 늘어난 만큼 늘릴 수도 있지만 핵심 작업이 아니라 보류했습니다.
- **완전히 빈 DB에서 콘텐츠/빈칸/미션이 하나도 안 만들어지는 버그를 발견해 함께 고쳤습니다.** `@Order` 를 `@Configuration` 클래스에 붙여왔는데, Spring Boot는 `CommandLineRunner` 실행 순서를 결정할 때 클래스의 `@Order` 는 보지 않고 `@Bean` 팩토리 메서드의 `@Order` 만 본다. 그래서 단원 시드(`UnitDataInitializer`)보다 콘텐츠/빈칸 시드가 먼저 실행되는 경우가 있었고, 이때는 `unitRepository.findByCode()` 가 아직 없는 단원을 찾지 못해 조용히(에러 없이) 아무것도 만들지 않았다. 볼륨을 한 번 지우고 새로 띄워 재현 후, `UnitDataInitializer`/`ContentDataInitializer`/`BlankDataInitializer`/`MissionDataInitializer` 네 곳 모두 `@Order` 를 `@Bean` 메서드로 옮겨 고쳤고, 완전히 빈 DB에서 한 번에 정상 시드되는 것을 확인했다.

## 구현 메모 (7단계 1/4 반영 — 잔디심기 · 서비스 좋아요)

- `activity_log` 구현. `ActivityLogService.record(userId)` 가 오늘 날짜 행을 upsert(없으면 생성, 있으면 count+1)한다. UNIQUE(user_id, activity_date).
- **기록 시점**은 "학습에 실질적 진전"이 있을 때만: (1) progress 스크롤이 실제 전진했을 때, (2) 빈칸을 **새로** 맞혔을 때(오답/이미 맞힌 문제 재제출 제외), (3) 실습 미션을 **처음** 완료했을 때. 단순 조회·오답은 기록하지 않아 잔디가 의미 없이 부풀지 않게 했다. 기존 ProgressService/BlankService/MissionService 에 호출 한 줄씩만 추가.
- 조회(GET /api/v1/activity)는 저장된 count 를 level(0~4)로 환산: 0 / 1~2 / 3~4 / 5~7 / 8+.
- `service_like` 구현. user_id 를 PK 로 사용(사용자당 1행). 총 개수는 `COUNT(*)`, 취소 시 행 삭제. GET(공개)은 총 개수+본인 여부, POST(인증)는 토글.

## 구현 메모 (6단계 반영)

- `community_post` / `community_comment` 구현. 작성자 닉네임/프로필은 복사 저장하지 않고 `user_id` 조인으로 응답에 채웁니다(설계 원칙 그대로 — 프로필 변경 즉시 과거 글/댓글에 반영).
- 답글은 **1단계 깊이만** 허용합니다. `parent_comment_id` 가 이미 답글인 댓글을 가리키면 400으로 거부합니다.
- 게시글 상세 조회 시 `view_count` 를 1 증가시킵니다.
- 삭제 규칙: 게시글 삭제 시 딸린 댓글/답글을 함께 삭제, 최상위 댓글 삭제 시 그 답글들을 함께 삭제.
- 권한: 게시글 수정=작성자, 게시글/댓글 삭제=작성자 또는 ADMIN, 댓글 수정=작성자 또는 ADMIN(프론트 UI는 수정 버튼을 작성자에게만 노출).
- 커뮤니티 API 는 열람 포함 전부 인증 필요. 비로그인 프론트 접근은 공통 `LoginPrompt`/`RequireAuth` 로 안내 후 로그인 유도(원 경로 복귀).

## 구현 메모 (5단계 반영)

- `practice_mission` 은 관리자 에디터 전까지 `DataInitializer` 로 시드합니다. 5단계에서는 리눅스 실습(linux-practice)에 SHELL 미션 4개를 넣었습니다. 나머지 실습 단원 미션은 이후 단계에서 추가합니다.
- `mission_type` 검증은 `MissionVerifier` 전략 + `MissionVerifierRegistry` 로 분기합니다. 5단계는 `ShellMissionVerifier`(정규식 매칭)만 등록되어 있고, 새 유형은 구현체 @Component 추가만으로 확장됩니다. **어떤 검증기도 사용자 입력을 실행하지 않습니다**(CLAUDE.md 원칙).
- `verify_pattern` 은 API 응답에 절대 노출하지 않습니다(치팅 방지). 정답 판정은 서버에서만 이뤄집니다.
- `mission_progress` 는 성공 시에만 완료 행을 upsert 합니다(오답은 저장하지 않음). XP 총합은 저장하지 않고 완료 미션의 `xp_reward` 합으로 조회 시점에 계산합니다(계산 가능한 값은 저장하지 않는 원칙).

## 다음에 정할 것

관리자 에디터에서 이미지 업로드 시 별도 `media` 테이블(파일 경로, 업로더, 연결된 post_id/content_id)을 둘지, 아니면 본문 HTML 안의 `<img src>` URL만으로 충분한지는 이미지 저장 방식(Docker volume vs 오브젝트 스토리지)을 정한 뒤 결정하는 게 좋습니다. 초기 MVP에서는 별도 테이블 없이 업로드된 파일을 volume 경로에 저장하고 URL만 본문에 삽입하는 방식으로 시작해도 충분합니다.
