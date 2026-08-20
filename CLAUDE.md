# CLAUDE.md — 클라우드 부트캠프 학습 플랫폼

이 문서는 Claude Code가 이 저장소에서 작업할 때 항상 참고하는 규칙 문서입니다. 저장소 루트에 두고, 내용이 실제와 달라지면 바로 갱신하세요.

## 프로젝트 개요

클라우드 부트캠프(리눅스, 쉘스크립트, 파이썬, 도커, K8s, AWS, 네트워크, 보안 등)에서 학습한 내용을 정리하고, 로드맵 기반 학습 진도 관리 + 실습 시뮬레이터 + 커뮤니티를 제공하는 학습 서비스입니다. 1인 개발 포트폴리오 프로젝트이며, 개발 초기에는 기능을 단순하고 명확하게 구현하는 것을 우선합니다.

## 기술 스택

아래 버전은 2026년 8월 기준 최신/권장 버전입니다. 실제 개발 시작 시점의 버전으로 조정해도 됩니다.

- Frontend: React 19 + JavaScript + Tailwind CSS + Node.js 24 LTS (또는 22 LTS) [[React 릴리스](https://react.dev/versions)] [[Node.js LTS 현황](https://endoflife.date/nodejs)]
- Backend: Spring Boot 3.5.16 (3.x 라인의 마지막 버전) + Java 21 (LTS). 3.x는 2026-06-30부로 공식 EOL이지만, 문서/튜토리얼/부트캠프 학습 자료와의 호환성을 우선해 의도적으로 선택함. 보안 패치가 더 나오지 않는다는 점은 감안하고 진행 [[Spring Boot 버전/EOL](https://www.herodevs.com/blog-posts/spring-boot-versions-eol-dates-and-latest-releases-april-2026)]
- DB: MariaDB (최신 안정 버전)
- 인증: JWT (Access Token + Refresh Token)
- 배포: Docker Compose (1차) → Kubernetes (2차)
- 문서/이슈: GitHub, Notion

버전을 바꿀 때는 이 섹션도 함께 수정해서 실제 `package.json` / `build.gradle`과 어긋나지 않게 유지하세요.

## 실행 명령어

```bash
# 프론트엔드 (frontend/ 디렉토리)
npm install
npm run dev          # 개발 서버
npm run build         # 프로덕션 빌드

# 백엔드 (backend/ 디렉토리)
./gradlew bootRun      # 개발 실행
./gradlew test         # 테스트
./gradlew build        # 빌드

# 전체 스택 (deploy/ 디렉토리)
docker compose -f docker-compose.yml up --build
```

## 폴더 구조 규칙

```
frontend/   React 앱. src/pages, src/components, src/api, src/store로 관심사 분리
backend/    Spring Boot 앱. 도메인별 패키지(user, unit, progress, mission, post, community, config)
deploy/     docker-compose.yml, docker-compose.prod.yml, k8s/ (yaml 매니페스트)
docs/       db-schema.md, api-spec.md, service-intro.md, dev-process.md
notes/      Notion에서 내보낸 학습 노트 원본. 학습 콘텐츠(content, blank_question)를 작성할 때 참고 자료로만 쓴다. 단원(unit.group_code)별로 하위 폴더를 나눠두면(예: notes/linux/, notes/docker/) 나중에 콘텐츠로 옮길 때 매핑하기 쉽다.
refers/     와이어프레임 등 기타 참고 자료.
logs/       런타임 로그 산출물.
```

`notes/`, `refers/`, `logs/`는 앱이 서빙하는 자산이 아니라 개인 참고 자료/런타임 산출물이므로 **첫 커밋 전에 `.gitignore`에 추가**한다. GitHub에는 올리지 않되, 로컬 저장소 안에는 그대로 둬서 Claude Code가 학습 콘텐츠를 작성할 때 참고할 수 있게 한다.

새 기능을 추가할 때 프론트는 `pages`(라우트 단위) → `components`(재사용 UI) → `api`(백엔드 호출)를 나눠서 구성하고, 백엔드는 도메인 패키지 안에 `controller / service / repository / dto`를 두는 구조를 따릅니다.

## 디자인 시스템

색상 팔레트(Tailwind `theme.extend.colors`에 등록해서 사용):

| 이름 | 값 | 용도(초안, 실제 배치는 자유) |
|---|---|---|
| accent | `#ECC815` | 강조/CTA |
| dark | `#162326` | 어두운 배경/텍스트 |
| primary | `#145D91` | 메인 브랜드 컬러 |
| secondary | `#56A5DD` | 보조 색상 |
| light | `#77B4E4` | 밝은 강조/hover |

로드맵 및 진도 아이콘 이미지는 `frontend/public/assets/imgs/roadmap/`에 둡니다. 새 학습 단원 아이콘을 추가할 때 이 경로 규칙을 유지하세요.

기본 폰트는 **Pretendard**입니다. 외부 CDN(Google Fonts 등)에 의존하지 않고 `frontend/public/assets/fonts/`에 폰트 파일을 두고 `@font-face`로 직접 서빙합니다(추후 배포 시 외부 의존성을 줄이기 위함).

전역 스크롤바는 두께 2px, 평소엔 옅게 / hover 시 진하게 표시하고, **레이아웃 공간을 차지하지 않는 오버레이 방식**으로 통일합니다(스크롤바 유무에 따라 페이지마다 요소 위치가 흔들리지 않도록 `scrollbar-gutter` 등으로 처리). 새 페이지/컴포넌트에서 이 스타일을 다시 정의하지 말고 전역 스타일을 상속받게 하세요.

## 라우팅 / 네비게이션

- `/` — 로드맵(메인홈). 안내 문구 없이 로드맵만 렌더링하며, 스크롤 없는 가로형 레이아웃입니다. 단원 요소들은 완전히 일렬로 정렬하지 않고 높이를 약간씩 다르게 배치해 자연스러운 느낌을 줍니다. 비로그인 사용자의 기본 진입 화면입니다.
- `/study` — 학습하기. 로드맵과는 별도 페이지이며, 상단에 "학습 로드맵 단원을 클릭해 학습을 시작하세요. 로그인하면 진도가 저장됩니다." 안내 문구를 둡니다. 로드맵의 그래픽/인터랙티브한 느낌과 달리, 단원과 그 안의 챕터(콘텐츠 단위)를 갤러리·블로그 글 목록처럼 구분해서 보여주는 "공부를 위한" 페이지로 구성합니다.
- 로그인 성공 시 기본 이동 대상은 `/dashboard`입니다. 다만 "로그인 필요 안내"(LoginPrompt)를 거쳐 로그인한 경우에는 원래 가려던 페이지로 복귀하는 것이 이 기본 규칙보다 우선합니다.

## 인증 / 보안 원칙

- Access Token은 짧은 만료(예: 30분), Refresh Token은 httpOnly 쿠키에 저장합니다. Access Token을 localStorage에 저장하지 않습니다(XSS 위험).
- 사용자 역할은 `USER` / `ADMIN` enum으로 관리합니다. 글 작성/수정/삭제, 학습 콘텐츠 관리는 `ADMIN`만 가능하도록 서버 측에서 검증합니다.
- 비밀번호는 반드시 BCrypt 등으로 해시하여 저장합니다. 평문 비밀번호를 코드/문서/커밋에 남기지 않습니다.
- 게시글 본문(WYSIWYG HTML)은 저장 전 반드시 서버 또는 클라이언트에서 sanitize(예: DOMPurify)합니다.
- **실습 콘텐츠 실행 원칙**: 백엔드는 사용자가 작성한 코드를 실제로 실행하지 않습니다. Python/SQL 실습은 브라우저 내 실행(Pyodide, sql.js)으로 처리하고, Docker/K8s 실습은 프론트 시뮬레이션 + 백엔드는 "정답 패턴 검증 API"만 제공합니다. 이 원칙을 벗어나 서버에서 임의 코드를 실행하는 기능은 추가하지 않습니다.
- **로그인 필요 안내 UX**: 로그인이 필요한 화면/액션에 비로그인 사용자가 접근하면 임의로 막거나 404를 띄우지 않고, 공통 컴포넌트(예: `LoginPrompt`)로 "로그인이 필요합니다" 안내와 확인 버튼을 보여준 뒤 확인 시 로그인 페이지로 이동시킵니다. 가능하면 로그인 후 원래 페이지로 돌아오게 합니다. 새 기능에서 로그인 유도가 필요하면 매번 새로 만들지 말고 이 컴포넌트를 재사용합니다.

## API 규칙

- REST 엔드포인트는 `/api/v1/...`로 시작합니다.
- 응답 포맷은 통일된 wrapper를 사용합니다: `{ "success": true, "data": ..., "message": null }` / 실패 시 `{ "success": false, "data": null, "message": "..." }`.
- 새 엔드포인트를 추가하면 `docs/api-spec.md`에 함께 기록합니다.
- 인증이 필요 없는 API(로드맵 조회, 비로그인 학습 열람 등)와 필요한 API(진도 저장, 커뮤니티 조회/글쓰기 등)를 명확히 구분해서 문서화합니다.

## 환경 변수

실제 값은 `.env`에만 두고 git에 커밋하지 않습니다. `.env.example`에는 키 이름만 남깁니다. 새 환경 변수를 추가하면 `frontend/.env.example`, `backend/.env.example`, `docs/env-vars.md`를 함께 업데이트합니다.

## 개발 단계 (진행 순서)

1. 인증(회원가입/로그인/JWT) + 사용자 CRUD — 완료
2. 로드맵(메인홈) 정적 렌더링 + 헤더 — 완료
3. 대시보드 뼈대(진도 표시, 잔디심기) — 더미 데이터로 우선 구현 — 완료(진도/잔디는 더미, 계정 관리만 실연동)
4. 학습 콘텐츠 페이지(일반 학습, 빈칸 채우기) + 진도 저장 연동 — 완료(콘텐츠는 일부 단원만 시드, 전체 채우기는 7단계에서 일괄 처리)
5. 실습 시뮬레이터(Python/DB/Docker/K8s) — 항목별로 순차 구현 — 진행 중(리눅스 실습만 완료, 나머지 6종 남음: 쉘 스크립트/서버 구축/파이썬/데이터베이스/도커/K8s)
6. 커뮤니티(글/댓글/답글) + 관리자 에디터 — 진행 중. **커뮤니티는 열람·작성 모두 로그인 필요**(비로그인 접근 시 위 "로그인 필요 안내 UX" 규칙 적용)
7. 배포 전 마무리 작업 — 아래 항목을 한 번에 처리:
   - 로드맵 진입 애니메이션(단원이 하나씩 나타나며 선으로 연결) + 마우스 따라 확대되는 효과
   - 잔디심기(activity_log) 실제 연동 (3단계 이후 더미로 남아있던 부분)
   - 학습 콘텐츠 전체 단원 채우기 (`notes/`의 노션 노트 기반으로 4단계에서 비워둔 나머지 단원 콘텐츠·빈칸 문제 작성)
   - 온보딩 투어(회원가입 직후 서비스 안내)
   - 서비스 좋아요 버튼(service_like)
8. Docker Compose 배포 → Kubernetes 전환

지금 몇 단계까지 진행되었는지는 이 섹션을 직접 갱신하거나, 세션 시작 시 알려주세요.

## 참고 문서

- `docs/db-schema.md` — 테이블 설계
- `docs/api-spec.md` — API 명세
- `docs/service-intro.md`, `docs/dev-process.md` — 서비스 소개 및 개발 기록
