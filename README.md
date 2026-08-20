# 클라우드 부트캠프 학습 플랫폼

클라우드 부트캠프(리눅스, 쉘스크립트, 파이썬, 도커, K8s, AWS, 네트워크, 보안 등) 학습 내용을
정리하고 로드맵 기반 진도 관리 + 실습 시뮬레이터 + 커뮤니티를 제공하는 학습 서비스입니다.
1인 개발 포트폴리오 프로젝트.

> 프로젝트 규칙은 루트의 [`CLAUDE.md`](./CLAUDE.md) 를 따릅니다.

## 기술 스택

| 영역 | 스택 |
|---|---|
| Frontend | React 19 + JavaScript + Vite + Tailwind CSS (Node.js 22/24 LTS) |
| Backend | Spring Boot 3.5.16 + Java 21 (Gradle) |
| DB | MariaDB |
| 인증 | JWT (Access + Refresh Token) |
| 배포 | Docker Compose → Kubernetes |

## 개발 진행 단계

현재: **1단계 — 인증(회원가입/로그인/JWT) + 사용자 CRUD**

전체 단계 순서는 `CLAUDE.md` 의 "개발 단계" 섹션 참고.

## 로컬 실행

### 1. 환경 변수 준비

```bash
cp .env.example .env
cp backend/.env.example backend/.env
cp frontend/.env.example frontend/.env
# .env 파일들의 change-me-* 값을 실제 값으로 채웁니다.
```

### 2. Docker Compose 로 전체 스택 실행 (권장)

```bash
docker compose -f deploy/docker-compose.yml --env-file .env up --build
```

- Frontend: http://localhost:5173
- Backend:  http://localhost:8080
- MariaDB:  localhost:3306

### 3. 개별 실행 (로컬 개발)

로컬에 Node.js 22+, JDK 21 이 설치되어 있어야 합니다.

```bash
# Backend (MariaDB 가 떠 있어야 함)
cd backend
./gradlew bootRun

# Frontend
cd frontend
npm install
npm run dev
```

## 폴더 구조

```
frontend/   React 앱 (pages / components / api / store / hooks / styles)
backend/    Spring Boot 앱 (도메인별 패키지: user, config, common ...)
deploy/     docker-compose, k8s 매니페스트
docs/       db-schema / api-spec / env-vars / service-intro / dev-process
notes/      노션에서 내보낸 학습 노트 (콘텐츠 원본)
refers/     와이어프레임 등 참고 문서
```

## 문서

- [`docs/db-schema.md`](./docs/db-schema.md) — 테이블 설계
- [`docs/api-spec.md`](./docs/api-spec.md) — API 명세
- [`docs/env-vars.md`](./docs/env-vars.md) — 환경 변수 목록
