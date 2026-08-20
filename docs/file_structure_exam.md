cloud-bootcamp-platform/
├── CLAUDE.md                      # Claude Code가 항상 참고하는 프로젝트 규칙 문서
├── README.md
├── .gitignore
├── .env.example                   # 실제 .env는 git에 올리지 않음
├── notes/                         # 노션에서 내보낸 학습 노트
├── refers/                        # 참고 문서(와이어프레임 등)
├── docs/
│   ├── service-intro.md           # 서비스 개요 및 사용법
│   ├── dev-process.md             # 개발 과정 기록
│   ├── db-schema.md               # ERD / 테이블 설계
│   ├── api-spec.md                # (또는 openapi.yaml) API 명세
│   └── env-vars.md                # 필요한 환경변수 목록 정리 (민감정보 X, 이름만)
├── frontend/
│   ├── public/
│   │   └── assets/
│   │       └── imgs/
│   │           └── roadmap/       # 로드맵/진도 아이콘 이미지
│   ├── src/
│   │   ├── api/                   # axios 등 API 클라이언트
│   │   ├── components/
│   │   ├── pages/
│   │   ├── hooks/
│   │   ├── store/                 # 전역 상태 관리
│   │   └── styles/
│   ├── package.json
│   ├── tailwind.config.js
│   └── .env.example
├── backend/
│   ├── src/main/java/.../
│   │   ├── config/                # Security, JWT, CORS 설정
│   │   ├── user/
│   │   ├── unit/                  # 학습 단원
│   │   ├── progress/              # 진도 관리
│   │   ├── mission/                # 실습 정답 검증
│   │   ├── post/                  # 게시글/댓글
│   │   └── common/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── application-prod.yml
│   ├── src/test/
│   ├── build.gradle
│   └── .env.example
├── deploy/
│   ├── docker-compose.yml
│   ├── docker-compose.prod.yml
│   ├── nginx/
│   └── k8s/
│       ├── frontend-deployment.yaml
│       ├── backend-deployment.yaml
│       ├── mariadb-statefulset.yaml
│       ├── ingress.yaml
│       └── secret.example.yaml
└── logs/                          # .gitignore 처리 (런타임 산출물)
