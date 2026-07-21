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