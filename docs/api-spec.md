# API 명세 — 클라우드 부트캠프 학습 플랫폼

모든 엔드포인트는 `/api/v1/...` 로 시작합니다.

## 공통 응답 포맷

성공:

```json
{ "success": true, "data": { }, "message": null }
```

실패:

```json
{ "success": false, "data": null, "message": "에러 메시지" }
```

## 인증 방식

- **Access Token**: 로그인/재발급 응답 body 의 `data.accessToken` 으로 내려갑니다.
  클라이언트는 `Authorization: Bearer <accessToken>` 헤더로 전송합니다.
  localStorage 저장 금지(XSS). 메모리(상태)에만 보관합니다.
- **Refresh Token**: httpOnly 쿠키(`refreshToken`) 로 내려가며, 재발급/로그아웃 시 사용됩니다.

---

## 1단계: 인증 + 사용자 CRUD

### 인증 필요 여부 표기
- 🔓 = 비로그인 접근 가능
- 🔒 = 인증 필요 (Access Token)
- 👑 = ADMIN 역할 필요

| Method | Path | 인증 | 설명 |
|---|---|---|---|
| POST | `/api/v1/auth/signup` | 🔓 | 회원가입 (가입 후 바로 로그인 처리) |
| POST | `/api/v1/auth/login` | 🔓 | 로그인 (Access 토큰 발급 + Refresh 쿠키 설정) |
| POST | `/api/v1/auth/refresh` | 🔓* | Refresh 쿠키로 Access 토큰 재발급 |
| POST | `/api/v1/auth/logout` | 🔒 | 로그아웃 (Refresh 토큰 무효화 + 쿠키 삭제) |
| GET | `/api/v1/users/me` | 🔒 | 내 정보 조회 |
| PATCH | `/api/v1/users/me` | 🔒 | 내 정보 수정 (닉네임, 프로필 이미지) |
| PATCH | `/api/v1/users/me/password` | 🔒 | 비밀번호 변경 |
| DELETE | `/api/v1/users/me` | 🔒 | 회원 탈퇴 |

`*` refresh 는 별도 Access 토큰 없이 쿠키만으로 호출.

---

### POST `/api/v1/auth/signup`

Request body:

```json
{
  "loginId": "guest2",
  "password": "1234",
  "nickname": "게스트2"
}
```

Response `data`:

```json
{
  "accessToken": "eyJhb...",
  "user": {
    "id": 3,
    "loginId": "guest2",
    "nickname": "게스트2",
    "profileImageUrl": null,
    "role": "USER"
  }
}
```

- 성공 시 `Set-Cookie: refreshToken=...; HttpOnly; Path=/; SameSite=Lax` 헤더 포함.
- `loginId` 중복 시 실패 응답(`message`: "이미 사용 중인 아이디입니다.").

### POST `/api/v1/auth/login`

Request body:

```json
{ "loginId": "admin", "password": "..." }
```

Response `data`: signup 과 동일 구조(accessToken + user).

### POST `/api/v1/auth/refresh`

- 요청: body 없음. `refreshToken` httpOnly 쿠키 필요.
- Response `data`: `{ "accessToken": "..." }` (필요 시 Refresh 쿠키 회전).

### POST `/api/v1/auth/logout`

- Refresh 토큰 무효화(`refresh_token_hash` = NULL) 및 쿠키 삭제.
- Response `data`: `null`.

### GET `/api/v1/users/me`

Response `data`:

```json
{
  "id": 2,
  "loginId": "guest",
  "nickname": "게스트",
  "profileImageUrl": null,
  "role": "USER",
  "createdAt": "2026-08-20T10:00:00"
}
```

### PATCH `/api/v1/users/me`

Request body(변경할 필드만):

```json
{ "nickname": "새닉네임", "profileImageUrl": "/assets/imgs/profile/xxx.png" }
```

Response `data`: 수정된 user 객체.

### PATCH `/api/v1/users/me/password`

```json
{ "currentPassword": "...", "newPassword": "..." }
```

Response `data`: `null`.

### DELETE `/api/v1/users/me`

Response `data`: `null`.

---

## 2단계: 로드맵(학습 단원)

| Method | Path | 인증 | 설명 |
|---|---|---|---|
| GET | `/api/v1/units` | 🔓 | 전체 학습 단원 목록 (sort_order 오름차순) |

### GET `/api/v1/units`

- 인증 불필요(비로그인 로드맵 열람용).
- `sort_order` 오름차순으로 전체 단원을 반환한다.

Response `data`:

```json
[
  {
    "id": 1,
    "code": "cloud-intro",
    "name": "클라우드 개론",
    "groupCode": "cloud",
    "type": "GENERAL",
    "iconImagePath": "/assets/imgs/roadmap/cloud-intro.png",
    "sortOrder": 1,
    "hasContent": true,
    "blankCount": 0,
    "missionCount": 0
  },
  {
    "id": 2,
    "code": "linux",
    "name": "리눅스",
    "groupCode": "linux",
    "type": "GENERAL",
    "iconImagePath": "/assets/imgs/roadmap/linux.png",
    "sortOrder": 2,
    "hasContent": true,
    "blankCount": 5,
    "missionCount": 0
  }
]
```

- `type`: `GENERAL`(일반 학습) / `PRACTICE`(실습).
- `iconImagePath`: 프론트가 그대로 `<img src>` 로 사용하는 웹 경로. 실제 파일은
  `frontend/public/assets/imgs/roadmap/{code}.png` 에 두면 되고, 없으면 프론트에서
  단원 이름 첫 글자 플레이스홀더로 대체한다.
- `hasContent`(bool) / `blankCount`(int) / `missionCount`(int): 학습하기(`/study`) 카드의 보조 정보용.
  저장하지 않고 조회 시점에 그룹 집계로 계산한다. 로드맵은 이 필드를 사용하지 않는다.
- 프론트 라우트 `/study`(학습하기)는 이 엔드포인트만 사용하며 별도 백엔드 API 는 없다.
- **(7단계 4/4) GENERAL 10개 단원 전체에 콘텐츠·빈칸 시드 완료** → 모든 GENERAL 단원이 `hasContent=true`,
  `blankCount>0` 으로 응답한다("그외"는 노트 없이 일반 지식으로 작성, 사용자 검토 대상).

---

## 4단계: 학습 콘텐츠 · 진도 · 빈칸 채우기

| Method | Path | 인증 | 설명 |
|---|---|---|---|
| GET | `/api/v1/units/{unitCode}/chapters` | 🔓 | 단원 챕터 목록(본문 제외) — 8단계 |
| GET | `/api/v1/units/{unitCode}/chapters/{sortOrder}` | 🔓 | 챕터 본문 + 이전/다음 — 8단계 |
| POST | `/api/v1/chapters/{chapterId}/visit` | 🔒 | 챕터 열람 기록(upsert) + 잔디 — 8단계 |
| GET | `/api/v1/progress` | 🔒 | 로그인 사용자의 GENERAL 단원별 진도 요약 |
| GET | `/api/v1/units/{code}/blanks` | 🔓 | 빈칸 문제 목록(로그인 시 이전 답안 포함) |
| POST | `/api/v1/blanks/{id}/answer` | 🔒 | 빈칸 답안 제출/채점 |

> **8단계 개선**: 통짜 본문 조회(`GET /units/{code}/content`)와 스크롤 저장(`PATCH /units/{code}/progress`)을
> 제거하고, 학습 본문을 **챕터**로 나눴습니다. 진도는 "방문한 챕터 수 / 전체 챕터 수" 로 계산합니다.

### GET `/api/v1/units/{unitCode}/chapters`

Response `data`: `[ { "id":1, "title":"패러다임의 전환", "sortOrder":1 }, ... ]`
- `sortOrder` 오름차순. 콘텐츠가 없는 단원은 빈 배열.

### GET `/api/v1/units/{unitCode}/chapters/{sortOrder}`

Response `data`:
```json
{ "id":1, "unitCode":"cloud-intro", "title":"패러다임의 전환", "sortOrder":1,
  "body":"<h2>...</h2>...",
  "prev": null,
  "next": { "sortOrder":2, "title":"온프레미스와 클라우드" } }
```
- `body` 는 HTML. 프론트는 렌더링 전 DOMPurify 로 sanitize 하고, h2/h3 를 스캔해 목차를 만든다.
- `prev`/`next` 는 없으면 `null`(`{ sortOrder, title }`).
- 존재하지 않는 단원/챕터면 404.

### POST `/api/v1/chapters/{chapterId}/visit`

- 로그인 사용자가 챕터를 열람했음을 기록. 이미 기록이 있으면 무시(upsert).
- **신규 방문일 때만** `activity_log`(잔디)를 갱신한다.
- Response `data`: `null`. 존재하지 않는 챕터면 404.

### GET `/api/v1/progress`

Response `data` (GENERAL 단원만):
```json
[ { "unitCode":"linux", "type":"GENERAL", "generalPercent":75, "blankPercent":60 } ]
```
- **`generalPercent` = 방문한 챕터 수 / 전체 챕터 수 × 100** (8단계 변경, 저장된 값 아님).
- `blankPercent` = 해당 단원 빈칸 문제 중 맞힌 비율(맞힌 수 / 전체 수 × 100).
- 응답 shape 은 이전과 동일(대시보드가 그대로 소비). PRACTICE 단원은 포함하지 않는다.

### GET `/api/v1/units/{code}/blanks`

Response `data`:
```json
[ { "id":1, "sentenceTemplate":"... {blank} ...", "score":10, "sortOrder":1,
    "userAnswer":"chmod", "isCorrect":true } ]
```
- **정답 문자열(answer)은 포함하지 않는다**(치팅 방지). 정답은 채점 응답에서만 노출.
- `userAnswer` / `isCorrect` 는 로그인 상태에서 이전 답안이 있을 때만 채워지고, 없으면 `null`.
- `{blank}` 위치를 프론트에서 `<input>` 으로 치환해 렌더링한다.

### POST `/api/v1/blanks/{id}/answer`

Request: `{ "answer": "chmod" }`
Response `data`: `{ "isCorrect": false, "correctAnswer": "chmod" }`
- 정답 비교는 앞뒤 공백/대소문자를 무시한다(학습 편의).
- upsert: 최초 제출은 생성, 이후는 답안/정답여부 갱신 + `attempts` 증가.
- 오답 UI(정답 회색 표시)를 위해 `correctAnswer` 를 함께 반환한다.

---

## 5단계: 실습 미션 (실습 시뮬레이터)

| Method | Path | 인증 | 설명 |
|---|---|---|---|
| GET | `/api/v1/units/{code}/missions` | 🔓 | PRACTICE 단원의 미션 목록 + XP 요약 (로그인 시 완료 여부 포함) |
| POST | `/api/v1/missions/{id}/verify` | 🔒 | 미션 정답 검증(패턴 매칭) + 성공 시 완료 저장 |

> 5단계에서는 `SHELL` 유형만 실제 검증을 구현합니다. 나머지 유형(PYTHON/DB/DOCKER/K8S)은
> 이후 단계에서 `MissionVerifier` 구현체를 추가하는 방식으로 확장됩니다.

### GET `/api/v1/units/{code}/missions`

Response `data`:
```json
{
  "missions": [
    { "id": 1, "title": "숨김 파일까지 모두 표시",
      "description": "현재 디렉토리의 숨김 파일까지 ...",
      "missionType": "SHELL", "xpReward": 50, "sortOrder": 1, "completed": false }
  ],
  "earnedXp": 0,
  "totalXp": 250
}
```
- **`verify_pattern`(정답 정규식)은 응답에 포함하지 않는다**(치팅 방지).
- `completed` 는 로그인 사용자의 완료 여부(비로그인 시 항상 false).
- `earnedXp`/`totalXp` 는 저장하지 않고 조회 시점에 계산한다(완료 미션 xp 합 / 전체 xp 합).

### POST `/api/v1/missions/{id}/verify`

Request: `{ "input": "ls -a" }`
Response `data`: `{ "correct": true, "completed": true, "xpReward": 50 }`
- 백엔드는 **사용자 입력을 실행하지 않고** `mission_type` 에 맞는 검증기(현재 SHELL=정규식 매칭)로
  `verify_pattern` 과 대조만 한다(CLAUDE.md 실습 콘텐츠 실행 원칙).
- 성공 시 `mission_progress` 를 upsert(완료 처리). 성공 명령의 "그럴듯한 출력"은 프론트에서 생성한다.
- `xpReward` 는 해당 미션의 보상 XP(성공 시 "+N XP" 표시용).

---

## 6단계: 커뮤니티 (게시글 / 댓글 / 답글)

> **커뮤니티는 열람·작성 모두 인증 필요**(🔒). 비로그인 접근 시 프론트는 공통 `LoginPrompt`
> 로 안내하고 로그인 페이지로 유도한 뒤, 로그인 성공 시 원래 경로로 복귀시킨다.
> 본문은 일반 텍스트(줄바꿈만). 답글은 1단계 깊이만 지원(답글에 답글 불가).

| Method | Path | 인증 | 설명 |
|---|---|---|---|
| GET | `/api/v1/posts` | 🔒 | 게시글 목록(최신순, 댓글 수 포함) |
| POST | `/api/v1/posts` | 🔒 | 게시글 작성(로그인 사용자 누구나) |
| GET | `/api/v1/posts/{id}` | 🔒 | 게시글 상세(조회 시 view_count +1) |
| PUT | `/api/v1/posts/{id}` | 🔒 | 게시글 수정(작성자 본인만) |
| DELETE | `/api/v1/posts/{id}` | 🔒 | 게시글 삭제(작성자 본인 또는 ADMIN, 딸린 댓글 함께 삭제) |
| GET | `/api/v1/posts/{id}/comments` | 🔒 | 댓글+답글 트리 |
| POST | `/api/v1/posts/{id}/comments` | 🔒 | 댓글/답글 작성 |
| PUT | `/api/v1/comments/{id}` | 🔒 | 댓글 수정(작성자 본인 또는 ADMIN) |
| DELETE | `/api/v1/comments/{id}` | 🔒 | 댓글 삭제(작성자 본인 또는 ADMIN) |
| GET | `/api/v1/users/me/posts` | 🔒 | 내가 쓴 글(대시보드) |
| GET | `/api/v1/users/me/comments` | 🔒 | 내가 쓴 댓글(대시보드) |

### POST `/api/v1/posts`
Request: `{ "title": "...", "body": "여러 줄\n본문" }`
Response `data`(상세): `{ id, title, body, authorId, authorNickname, authorProfileImageUrl, viewCount, createdAt, updatedAt }`

### GET `/api/v1/posts` (목록 항목)
`{ id, title, authorId, authorNickname, viewCount, commentCount, createdAt }`
- 작성자 닉네임/프로필은 저장 시 복사하지 않고 `user_id` 조인으로 채운다(프로필 변경 즉시 과거 글에도 반영).

### POST `/api/v1/posts/{id}/comments`
Request: `{ "body": "...", "parentCommentId": null | 최상위댓글id }`
- `parentCommentId` 가 답글(대댓글)을 가리키면 400("답글에는 답글을 달 수 없습니다.").

### GET `/api/v1/posts/{id}/comments` (트리)
```json
[ { "id":1, "body":"...", "authorId":2, "authorNickname":"게스트",
    "authorProfileImageUrl":null, "parentCommentId":null,
    "createdAt":"...", "updatedAt":"...",
    "replies":[ { "id":3, "parentCommentId":1, "replies":[], ... } ] } ]
```

- 권한: 게시글 수정=작성자, 게시글 삭제=작성자/ADMIN, 댓글 수정·삭제=작성자/ADMIN. 서버에서 최종 검증(403).
- SecurityConfig: `/api/v1/posts/**`, `/api/v1/comments/**` 는 공개 목록에 넣지 않고 명시적으로 `authenticated()`.

---

## 7단계(1/4): 잔디심기(activity_log) · 서비스 좋아요(service_like)

| Method | Path | 인증 | 설명 |
|---|---|---|---|
| GET | `/api/v1/activity` | 🔒 | 최근 6개월 날짜별 활동(잔디) |
| GET | `/api/v1/service-like` | 🔓 | 서비스 좋아요 총 개수 + 내가 눌렀는지 |
| POST | `/api/v1/service-like` | 🔒 | 좋아요 토글(있으면 취소, 없으면 생성) |

### GET `/api/v1/activity`

Response `data` (일요일 정렬 시작 ~ 오늘, 오름차순):
```json
[ { "date": "2026-03-01", "level": 0, "count": 0 },
  { "date": "2026-03-02", "level": 2, "count": 3 } ]
```
- `level` = count 구간 → 0 / 1~2(1) / 3~4(2) / 5~7(3) / 8+(4).
- 3단계 프론트 `mockActivity.js` 와 동일 shape(대시보드 GrassSection 이 그대로 사용).
- **활동 기록 시점**(activity_count 증가): 진도가 실제로 전진했을 때 / 빈칸을 새로 맞혔을 때 /
  실습 미션을 처음 완료했을 때. 단순 조회·오답·재제출은 기록하지 않는다(잔디 인플레이션 방지).

### GET `/api/v1/service-like`

Response `data`: `{ "totalCount": 3, "likedByMe": false }`
- 비로그인 시 `likedByMe` 는 항상 false. 총 개수는 `COUNT(*)` 로 계산(저장하지 않음).

### POST `/api/v1/service-like`

- 토글: 이미 눌렀으면 삭제(취소), 아니면 생성(좋아요).
- Response `data`: 토글 후 `{ totalCount, likedByMe }`.

---

## 8단계: 실습 단원 노트 (신규 엔드포인트 없음)

8단계에서 7개 PRACTICE 단원(`linux-practice`, `shell-practice`, `server-build-practice`,
`python-practice`, `database-practice`, `docker-practice`, `k8s-practice`)에 **실습 노트 콘텐츠**를
`content` 테이블에 시드했습니다. 새 API 는 없고 기존 콘텐츠 API 를 그대로 재사용합니다.

- 챕터 API(`GET /api/v1/units/{code}/chapters`)는 **type 과 무관**하게 동작하므로, PRACTICE 단원 code 로도
  실습 노트 챕터를 반환합니다(예: `docker-practice`). (8단계 후반 개선으로 통짜 `content` 조회는 챕터로 대체됨.)
- 프론트: 리눅스 실습(미션 있음)은 미션 풀이 화면 하단에, 미션이 없는 6개 실습 단원은 "실습 기능 준비 중"
  안내와 함께, 챕터 본문들을 이어붙여 블로그 형식으로 렌더링합니다(`PracticeNoteSection`, `missionCount === 0` 분기).
- 이미지: 노트 이미지는 `frontend/public/assets/imgs/notes/{code}/` 로 복사되어 본문 `<img src>` 가
  이 웹 경로를 가리킵니다. 인증 불필요(🔓, 비로그인 열람 허용) — 기존 콘텐츠 API 정책과 동일.

> 이후 단계(나머지 실습 유형의 인터랙티브 기능, HTTPS 등)의 엔드포인트는 해당 단계 구현 시 이 문서에 추가합니다.
