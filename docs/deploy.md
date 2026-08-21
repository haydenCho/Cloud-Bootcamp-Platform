# 배포 런북 — 클라우드 부트캠프 학습 플랫폼 (lib.solcho.com)

이 문서는 **운영 서버에서 그대로 따라 하면 되는** 배포 절차입니다. 대상 도메인은 `lib.solcho.com`,
1차 배포는 **HTTP(80)** 기준입니다. HTTPS(443)는 마지막 섹션(다음 단계)에서 certbot 으로 추가합니다.

## 아키텍처 개요

```
브라우저 ── http://lib.solcho.com (80) ──▶ [frontend 컨테이너 = nginx]
                                              ├─ /            → 정적 SPA(dist) 서빙
                                              └─ /api/*        → http://backend:8080 (리버스 프록시)
                                                                    │
                                        [backend 컨테이너 :8080] ──▶ [mariadb 컨테이너 :3306]
                                        (외부 포트 미노출)             (외부 포트 미노출, 내부 전용)
```

- **외부에 열리는 포트는 80 하나뿐**입니다. backend/mariadb 는 compose 내부 네트워크에서 서비스명으로만 접근합니다.
- 프론트와 API 가 **같은 출처**(`lib.solcho.com`)로 서빙되므로 CORS 는 사실상 트리거되지 않고, refresh 쿠키는
  `SameSite=Lax` + `Secure=false`(HTTP) 로 동작합니다.

관련 파일:
- `deploy/docker-compose.prod.yml` — 운영 스택 정의
- `deploy/nginx/default.conf` — nginx(정적 서빙 + /api 프록시, certbot 뼈대 포함)
- `.env.production.example` — 배포 환경변수 예시(복사해서 `.env.production` 작성)

---

## 0. 사전 요구사항 (서버)

```bash
# Docker / Docker Compose 설치 확인
docker --version
docker compose version        # v2 (플러그인) 필요. 없으면 아래로 설치.

# (미설치 시, Ubuntu 기준) Docker Engine + compose 플러그인 설치
curl -fsSL https://get.docker.com | sh
sudo usermod -aG docker "$USER"   # 로그아웃 후 재로그인하면 sudo 없이 docker 사용
```

## 1. 코드 가져오기

```bash
# 저장소 clone (또는 기존 디렉토리에서 git pull)
git clone <repo-url> cloud-bootcamp-platform
cd cloud-bootcamp-platform
```

> 코드 전달이 git 이 아니라면(예: 압축본), 저장소 루트 전체를 서버에 복사하면 됩니다.
> **주의**: 앱이 서빙하는 이미지(`frontend/public/assets/imgs/notes/`)는 커밋/전달 대상입니다.
> 반면 루트 `notes/`, `refers/` 는 `.gitignore` 로 제외됩니다(참고 자료).

## 2. 환경변수 파일 작성

```bash
cp .env.production.example .env.production
# 편집기로 열어 change-me 값들을 실제 값으로 교체
#  - MARIADB_PASSWORD / MARIADB_ROOT_PASSWORD : 강한 무작위 문자열
#  - JWT_SECRET : openssl rand -base64 48
#  - ADMIN_PASSWORD : 관리자 로그인 비밀번호
#  - CORS_ALLOWED_ORIGINS / VITE_API_BASE_URL : http://lib.solcho.com (도메인 그대로)
#  - COOKIE_SECURE=false (HTTP), HTTPS 전환 후 true
nano .env.production
```

`.env.production` 은 절대 커밋하지 않습니다(`.gitignore` 로 제외됨).

## 3. 빌드 & 기동

```bash
docker compose -f deploy/docker-compose.prod.yml --env-file .env.production up -d --build
```

- 최초 실행은 backend(gradle)·frontend(vite) 빌드와 mariadb 이미지 pull 로 수 분 걸립니다.
- 상태 확인:

```bash
docker compose -f deploy/docker-compose.prod.yml --env-file .env.production ps
docker compose -f deploy/docker-compose.prod.yml --env-file .env.production logs -f backend
```

- 3개 서비스 컨테이너(frontend / backend / mariadb)가 떠 있고, mariadb 는 `healthy`,
  backend 로그에 단원/콘텐츠 시드 로그가 보이면 정상입니다.
  (compose 가 컨테이너 이름을 `<project>-<service>-1` 형태로 자동 생성합니다.
   같은 서버에서 개발 스택과 함께 돌린다면 `-p bootcamp-prod` 처럼 프로젝트명을 분리하세요.)

## 4. 동작 확인

```bash
# 서버 내부에서
curl -I http://localhost/                         # 200 + text/html (SPA)
curl -s http://localhost/api/v1/units | head      # {"success":true,"data":[...]} (JSON)

# 도메인으로 (DNS/hosts 가 서버를 가리키면)
curl -I http://lib.solcho.com/
```

브라우저에서 `http://lib.solcho.com` 접속 → 로드맵이 보이고, 학습/실습 단원에서 이미지가 로드되면 성공입니다.

## 5. DNS & 방화벽 점검

- **DNS**: `lib.solcho.com` 의 A 레코드가 이 서버의 **공인 IP** 를 가리키는지 확인.
  ```bash
  dig +short lib.solcho.com          # 또는 nslookup lib.solcho.com
  curl -s ifconfig.me; echo          # 이 서버의 공인 IP
  # 위 두 값이 같아야 외부에서 도메인으로 접속됩니다.
  ```
- **방화벽(80 포트 개방)**:
  ```bash
  # ufw 사용 시
  sudo ufw allow 80/tcp && sudo ufw status
  # firewalld 사용 시
  sudo firewall-cmd --add-service=http --permanent && sudo firewall-cmd --reload
  # 클라우드(예: AWS/GCP)면 보안 그룹/방화벽 규칙에서 TCP 80 인바운드 허용도 확인
  ```
- 포트 리슨 확인: `sudo ss -ltnp | grep ':80'` (docker-proxy 가 0.0.0.0:80 리슨)

## 6. 갱신 배포 (재배포)

```bash
git pull
docker compose -f deploy/docker-compose.prod.yml --env-file .env.production up -d --build
```

DB 데이터는 `mariadb-data` 볼륨에 유지됩니다. 콘텐츠 HTML(`backend/src/main/resources/content/*.html`)이 바뀌면
`ContentDataInitializer` 가 기동 시 DB 본문과 파일을 비교해 **다를 때만** 갱신합니다(idempotent).

## 7. 정지 / 초기화

```bash
# 정지(데이터 유지)
docker compose -f deploy/docker-compose.prod.yml --env-file .env.production down
# 정지 + DB 볼륨까지 삭제(주의: 데이터 소실)
docker compose -f deploy/docker-compose.prod.yml --env-file .env.production down -v
```

---

## 다음 단계 — HTTPS(443) 추가 권장 (Let's Encrypt / certbot)

이번 배포 범위는 HTTP(80) 까지입니다. `deploy/nginx/default.conf` 에 ACME challenge 경로와 443 server 블록을
**주석으로 미리 구조화**해 두었으므로, 아래 순서로 쉽게 HTTPS 를 붙일 수 있습니다.

1. certbot 컨테이너/패키지로 인증서 발급 (webroot 방식 권장):
   - `deploy/nginx/default.conf` 의 `location /.well-known/acme-challenge/` 블록 주석 해제 후
     `/var/www/certbot` 볼륨을 frontend(nginx) 와 certbot 컨테이너에 공유 마운트.
   - certbot 실행:
     ```bash
     docker run --rm -v certbot-www:/var/www/certbot -v certbot-conf:/etc/letsencrypt \
       certbot/certbot certonly --webroot -w /var/www/certbot -d lib.solcho.com --email you@example.com --agree-tos
     ```
2. `default.conf` 하단의 **443 server 블록 주석을 해제**하고, 80 블록은 ACME + https 리다이렉트만 남긴다.
3. 인증서 볼륨(`/etc/letsencrypt`)을 nginx 컨테이너에 마운트하도록 `docker-compose.prod.yml` 에 추가.
4. 백엔드 쿠키를 HTTPS 에 맞춘다: `.env.production` 에서 `COOKIE_SECURE=true` 로 변경 후 재기동
   (코드 변경 불필요 — 환경변수만 교체).
5. 갱신 자동화: certbot `renew` 를 cron/타이머로 등록하고 nginx `reload`.
