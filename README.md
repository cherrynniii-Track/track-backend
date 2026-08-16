# Track Backend

프로젝트와 작업을 기록하고 진행 상태를 추적하는 REST API 서버다. 회원은 JWT 기반으로 인증하며, 자신이 소유한 프로젝트 안에서 카테고리와 작업을 관리할 수 있다. 작업은 상태·난이도·우선순위·마감일·카테고리 조건으로 조회할 수 있고, 대시보드에서는 프로젝트별 작업 통계를 제공한다. 마감이 임박한 작업은 주기적으로 탐색해 이메일 알림을 발송하며, 중복 발송 이력을 관리한다.

## 주요 기능

- 회원가입 및 로그인, Access/Refresh Token 발급
- 프로젝트·카테고리·작업 CRUD와 소유자 권한 검증
- 작업 조건 검색, 마감일 순 정렬 및 페이지네이션
- 상태별·난이도별·전체·기한 초과 작업 통계 대시보드
- Redis 기반 대시보드 캐시
- 스케줄러와 Kafka를 이용한 비동기 마감 임박 이메일 알림
- 알림 발송 이력 관리, 중복 방지 및 실패 재시도
- Swagger UI/OpenAPI 문서와 Actuator 헬스체크

## 기술 스택

| 구분 | 기술 |
| --- | --- |
| Language | Java 21 |
| Framework | Spring Boot 3.5, Spring Web MVC |
| Data | Spring Data JPA, Hibernate, MySQL 8.0 |
| Cache | Spring Cache, Redis 7 |
| Security | Spring Security, JWT, BCrypt |
| Messaging | Apache Kafka, Spring Kafka |
| Notification | Spring Scheduler, Spring Mail (Gmail SMTP) |
| API/Monitoring | springdoc-openapi, Spring Boot Actuator, p6spy |
| Test | JUnit 5, Spring Boot Test, Testcontainers, k6 |
| Build/Runtime | Gradle, Docker, Docker Compose |
| CI/CD | GitHub Actions, GitHub Container Registry, AWS EC2 |
| Orchestration manifests | Kubernetes |

## 애플리케이션 구조

```text
Client
  └─ Controller (REST API)
       └─ Service (인증·권한·트랜잭션·비즈니스 규칙)
            ├─ Repository (Spring Data JPA) ─ MySQL
            ├─ Cache (Spring Cache) ───────── Redis
            ├─ Scheduler ── Kafka Producer ── Kafka
            └─ Kafka Consumer ── Mail ─────── SMTP
```

핵심 도메인은 `Member → Project → Task` 구조이며, `Category`와 `Task`는 다대다 관계다. 모든 프로젝트·카테고리·작업 API는 인증된 회원이 해당 프로젝트의 소유자인지 검증한다.

## Docker 실행 방법

### 1. Docker 환경변수 설정

프로젝트 루트에 `.env` 파일을 생성하고 MySQL 컨테이너 초기화 정보를 설정한다.

```env
MYSQL_DATABASE=track
MYSQL_USER=YOUR_DB_USERNAME
MYSQL_PASSWORD=YOUR_DB_PASSWORD
MYSQL_ROOT_PASSWORD=YOUR_ROOT_PASSWORD
```

프로젝트 루트에 `.env.docker` 파일을 생성하고 애플리케이션의 접속 정보를 설정한다.

```env
SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/track
SPRING_DATASOURCE_USERNAME=YOUR_DB_USERNAME
SPRING_DATASOURCE_PASSWORD=YOUR_DB_PASSWORD

SPRING_DATA_REDIS_HOST=redis
SPRING_DATA_REDIS_PORT=6379

JWT_SECRET=YOUR_SUFFICIENTLY_LONG_JWT_SECRET

MAIL_USERNAME=email
MAIL_PASSWORD=email_app_password
```

`.env`와 `.env.docker`에는 민감한 정보가 포함되므로 Git에 커밋하지 않는다.

### 2. 전체 컨테이너 빌드 및 실행

```bash
docker compose up -d --build
```

위 명령어로 다음 컨테이너가 함께 실행된다.

- `track-mysql`
- `track-redis`
- `track-app`

### 3. 실행 상태 확인

```bash
docker compose ps
```

애플리케이션 로그를 확인한다.

```bash
docker compose logs --tail 50 app
```

로그에 다음 문구가 출력되면 정상적으로 실행된 것이다.

```text
Started TrackApplication
```

수동 헬스체크 방법은 다음과 같다.

```bash
curl -i http://localhost:8080/actuator/health
```

### 4. 컨테이너 종료

```bash
docker compose down
```

컨테이너와 MySQL 데이터를 모두 삭제하려면 다음 명령어를 사용한다.

```bash
docker compose down -v
```

> `-v` 옵션을 사용하면 MySQL 데이터가 저장된 볼륨도 삭제되므로 주의한다.

### 5. 변경사항 반영 후 다시 실행

애플리케이션 코드를 수정한 뒤 이미지를 다시 빌드하려면 다음 명령어를 사용한다.

```bash
docker compose up -d --build
```

## 클라우드 인프라 구조

현재 자동 배포 환경은 AWS EC2 한 대에서 Docker Compose로 애플리케이션과 의존 서비스를 운영하는 구조다. 애플리케이션 이미지는 GitHub Container Registry(GHCR)에 저장되며, MySQL과 Kafka 데이터는 Docker named volume에 보존된다.

```text
Developer
   │ push to main
   ▼
GitHub Actions ── build/push ──▶ GHCR
   │                               │
   └──────── SSH deploy ───────────┘
                   │ pull
                   ▼
             AWS EC2 / Docker Compose
             ├─ track-app   :8080
             ├─ MySQL 8.0   :3307 → :3306
             ├─ Redis 7     :6379
             └─ Kafka       :9092 / :29092
                    │
                    └─ Gmail SMTP (마감 알림)
```

저장소의 `k8s/` 디렉터리에는 별도의 Kubernetes 실행 구성이 준비되어 있다. `track` 네임스페이스 안에 애플리케이션, MySQL, Redis Deployment와 각 ClusterIP Service를 구성하며, MySQL은 1Gi PVC를 사용한다. 민감 정보는 `track-app-secret` Kubernetes Secret을 통해 주입한다. 이 매니페스트는 현재 GitHub Actions의 EC2 자동 배포 경로와는 별도다.

> 현재 `k8s/` 애플리케이션 매니페스트에는 Kafka 리소스와 `KAFKA_BOOTSTRAP_SERVERS` 설정이 포함되어 있지 않다. Kafka 기능까지 Kubernetes에서 운영하려면 해당 구성을 추가해야 한다.

## 배포 흐름

### CI

`main` 브랜치 push 또는 Pull Request가 생성되면 `.github/workflows/ci.yml`이 실행된다.

1. 저장소 코드를 체크아웃한다.
2. Temurin JDK 21을 설정한다.
3. `./gradlew test`로 테스트를 수행한다.

통합 테스트는 Testcontainers의 MySQL 컨테이너를 이용하므로 실제 데이터베이스에 가까운 환경에서 검증한다.

### CD

`main` 브랜치에 push되면 `.github/workflows/cd.yml`이 다음 순서로 배포한다.

1. Docker Buildx와 GitHub Actions 캐시를 이용해 멀티 스테이지 이미지를 빌드한다.
2. `ghcr.io/cherrynniii-track/track-backend:latest` 태그로 GHCR에 push한다.
3. GitHub Secrets의 EC2 접속 정보를 이용해 SSH로 서버에 접속한다.
4. `/opt/track`에서 `git pull --ff-only origin main`을 수행한다.
5. 새 애플리케이션 이미지를 pull하고 `docker compose up -d --no-build app`으로 앱 컨테이너를 교체한다.
6. 최대 120초 동안 `/actuator/health`를 5초 간격으로 검사한다.
7. 헬스체크가 실패하면 최근 애플리케이션 로그 100줄을 출력하고 배포를 실패 처리한다.

동일 브랜치의 배포가 중복 실행되면 진행 중인 이전 작업을 취소해 최신 커밋만 배포한다.

## 성능 최적화

### 조회 인덱스

`task` 테이블에는 실제 조회 패턴에 맞춘 다음 복합 인덱스가 선언되어 있다.

| 인덱스 | 컬럼 | 대상 조회 |
| --- | --- | --- |
| `idx_task_project_due_task` | `project_id, due_date, task_id` | 프로젝트별 목록의 마감일·ID 정렬 및 페이지 조회 |
| `idx_task_project_status_difficulty_due_task` | `project_id, status, difficulty, due_date, task_id` | 상태·난이도 조건이 포함된 목록 조회 |
| `idx_task_due_date_status` | `due_date, status` | 마감 임박 작업 탐색 |

인덱스의 선두 컬럼을 프로젝트와 마감일 등 주요 필터에 맞추고, 정렬의 동률을 `task_id`로 해소한다. 작업 목록은 한 요청에서 최대 100개까지만 허용해 과도한 조회와 연관 데이터 로딩을 제한한다.

### N+1 문제 해결

- 작업 목록을 DTO로 변환할 때 각 작업의 카테고리 컬렉션을 개별 조회하는 N+1 문제를 줄이기 위해 `Task.categories`에 `@BatchSize(size = 100)`을 적용했다. 페이지 최대 크기와 배치 크기를 동일하게 맞춰, 페이지 내 카테고리를 배치 쿼리로 로딩한다.
- 마감 알림 대상 조회는 Task → Project → Member를 JPQL `join fetch`로 한 번에 가져온다. 알림 수신 이메일과 프로젝트명을 접근할 때 작업마다 추가 쿼리가 발생하지 않는다.
- 카테고리 ID 검증은 중복 ID를 제거한 뒤 `findAllById`로 일괄 조회한다.

### 페이지네이션과 집계 쿼리

- 목록 API는 `Pageable`을 사용하고 데이터 조회 쿼리와 count 쿼리를 분리한다.
- 상태·난이도·우선순위·카테고리·마감일 범위 조건을 DB에서 처리하며, 마감일과 ID 순으로 일관되게 정렬한다.
- 대시보드의 상태별·난이도별 통계는 엔티티 전체를 메모리에 올리지 않고 `GROUP BY`와 인터페이스 프로젝션으로 집계한다.

### Redis 캐시

- 프로젝트별 대시보드 결과를 `dashboard::{projectId}` 키로 Redis에 저장하며 TTL은 10분이다.
- `sync = true`를 적용해 동일 키의 캐시 미스가 동시에 발생할 때 중복 집계 실행을 줄인다.
- 작업 생성·수정·삭제 시 해당 프로젝트 캐시를 즉시 제거해 변경 전 통계가 노출되지 않도록 한다.

### 알림 처리 안정성

- 스케줄러는 설정된 알림 시간 범위 안의 미완료 작업을 조회하고, 각 작업의 `TaskDueSoonEvent`를 `task-notification-events` 토픽에 발행한다.
- 이벤트에는 이벤트 ID, 작업 ID·제목, 프로젝트명, 마감일, 수신 이메일과 발생 시각을 담는다. 작업 ID를 Kafka 메시지 키로 사용해 같은 작업의 이벤트가 동일 파티션에서 처리되도록 한다.
- `task-email-notification-group` Consumer가 이벤트를 소비해 이메일을 발송하고 알림 이력을 갱신한다. 스케줄러의 작업 탐색과 외부 SMTP 통신을 분리해 이메일 발송이 스케줄러 실행을 직접 지연시키지 않는다.
- `(task_id, due_date)` 유니크 제약으로 같은 마감 알림의 중복 이력 생성을 DB 수준에서 방지한다.
- 이벤트 발행 전에 이력을 `PENDING`으로 생성하고, Consumer의 이메일 발송 결과에 따라 `SENT` 또는 `FAILED`로 변경한다. 이후 스케줄 실행에서는 `FAILED` 상태의 알림만 다시 발행한다.
- 이력 상태 변경을 짧은 개별 트랜잭션으로 처리해 이메일 I/O 동안 DB 트랜잭션을 오래 유지하지 않는다.

성능 회귀 확인을 위한 k6 시나리오는 `k6/task-list-baseline.js`에 있으며, 첫 페이지·복합 조건·카테고리·깊은 페이지 조회의 응답 시간을 각각 측정한다.
