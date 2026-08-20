# 환경 변수 목록

실제 값은 `.env` 에만 두고 git 에 커밋하지 않습니다. 여기에는 **키 이름과 용도만** 기록합니다.
새 환경 변수를 추가하면 `frontend/.env.example`, `backend/.env.example`, 그리고 이 문서를 함께 갱신합니다.

## Backend (`backend/.env`)

| 키 | 용도 | 예시/비고 |
|---|---|---|
| `DB_HOST` | MariaDB 호스트 | docker: `mariadb`, 로컬: `localhost` |
| `DB_PORT` | MariaDB 포트 | `3306` |
| `DB_NAME` | 데이터베이스 이름 | `bootcamp` |
| `DB_USERNAME` | DB 접속 계정 | |
| `DB_PASSWORD` | DB 접속 비밀번호 | 민감 |
| `JWT_SECRET` | JWT 서명 시크릿 | 32바이트 이상 무작위 값, 민감 |
| `JWT_ACCESS_TOKEN_VALIDITY_MINUTES` | Access 토큰 만료(분) | 기본 30 |
| `JWT_REFRESH_TOKEN_VALIDITY_DAYS` | Refresh 토큰 만료(일) | 기본 14 |
| `ADMIN_LOGIN_ID` | 초기 관리자 로그인 아이디 | 기본 `admin` |
| `ADMIN_PASSWORD` | 초기 관리자 비밀번호 | 민감, 코드 하드코딩 금지 |
| `ADMIN_NICKNAME` | 초기 관리자 닉네임 | |
| `GUEST_LOGIN_ID` | 시연용 게스트 아이디 | 기본 `guest` |
| `GUEST_PASSWORD` | 시연용 게스트 비밀번호 | 기본 `1234` |
| `GUEST_NICKNAME` | 시연용 게스트 닉네임 | |
| `CORS_ALLOWED_ORIGINS` | CORS 허용 오리진(쉼표 구분) | `http://localhost:5173` |

## Frontend (`frontend/.env`)

| 키 | 용도 | 비고 |
|---|---|---|
| `VITE_API_BASE_URL` | 백엔드 API 기본 URL | `http://localhost:8080` |

## Docker Compose (루트 `.env`)

| 키 | 용도 |
|---|---|
| `MARIADB_DATABASE` | 초기 생성 DB 이름 |
| `MARIADB_USER` | 초기 생성 DB 계정 |
| `MARIADB_PASSWORD` | 초기 생성 DB 계정 비밀번호 |
| `MARIADB_ROOT_PASSWORD` | MariaDB root 비밀번호 |

> 참고: 루트 `.env` 는 docker compose 가 `backend` 컨테이너로 `DB_*`, `JWT_*`, `ADMIN_*`, `GUEST_*` 값을
> 전달하는 데도 사용됩니다. 로컬에서 앱을 따로 실행할 때는 `backend/.env`, `frontend/.env` 를 사용하세요.
