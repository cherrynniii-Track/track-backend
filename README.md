## Docker 실행 방법

### 1. MySQL 및 Redis 실행

```bash
docker compose up -d
```

컨테이너의 실행 상태를 확인한다.

```bash
docker ps
```

다음 컨테이너가 실행 중이어야 한다.

- `track-mysql`
- `track-redis`

### 2. Docker 환경변수 설정

프로젝트 루트에 `.env.docker` 파일을 생성한다.

```env
SPRING_DATASOURCE_URL=jdbc:mysql://track-mysql:3306/track
SPRING_DATASOURCE_USERNAME=YOUR_DB_USERNAME
SPRING_DATASOURCE_PASSWORD=YOUR_DB_PASSWORD

SPRING_DATA_REDIS_HOST=track-redis
SPRING_DATA_REDIS_PORT=6379
```

`.env.docker`에는 민감한 정보가 포함되므로 Git에 커밋하지 않는다.

### 3. 애플리케이션 이미지 빌드

```bash
docker build -t track .
```

### 4. 애플리케이션 컨테이너 실행

MySQL과 Redis가 연결된 `track_default` 네트워크에서 실행한다.

```bash
docker run -d --name track-app \
  -p 8080:8080 \
  --network track_default \
  --env-file .env.docker \
  track
```

### 5. 실행 확인

```bash
docker logs --tail 50 track-app
```

로그에 다음 문구가 출력되면 정상적으로 실행된 것이다.

```text
Started TrackApplication
```

### 6. 컨테이너 종료 및 삭제

```bash
docker stop track-app
docker rm track-app
```

기존 컨테이너를 삭제하고 다시 실행하려면 다음 명령어를 사용한다.

```bash
docker rm -f track-app
```