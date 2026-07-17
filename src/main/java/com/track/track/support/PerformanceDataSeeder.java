package com.track.track.support;

import com.track.track.domain.Category;
import com.track.track.domain.Member;
import com.track.track.domain.Project;
import com.track.track.enums.Role;
import com.track.track.enums.task.TaskDifficulty;
import com.track.track.enums.task.TaskPriority;
import com.track.track.enums.task.TaskStatus;
import com.track.track.repository.CategoryRepository;
import com.track.track.repository.MemberRepository;
import com.track.track.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Task 목록 조회 성능 측정에 사용할 대량의 테스트 데이터를 준비한다
 * perf 프로필이 활성화되고 perf.seed.enabled=true 인 경우에만 실행
 * 이미 정상적으로 생성된 데이터가 있으면 다시 생성하지 않는다
 */
@Slf4j
@Component
@Profile("perf")
@ConditionalOnProperty(
        name = "perf.seed.enabled",
        havingValue = "true"
)
@RequiredArgsConstructor
public class PerformanceDataSeeder implements CommandLineRunner {

    private static final String TEST_EMAIL = "performance@test.com";
    private static final String TEST_PASSWORD = "performance123!";
    private static final String TEST_NICKNAME = "성능테스트";
    private static final List<String> CATEGORY_NAMES = List.of(
            "백엔드",
            "프론트엔드",
            "디자인",
            "기획",
            "리서치",
            "테스트",
            "문서화",
            "유지보수"
    );
    private static final int TOTAL_TASK_COUNT = 100000;
    private static final int BATCH_SIZE = 1000;
    private static final LocalDateTime BASE_DATE = LocalDateTime.of(2026, 7, 17, 0, 0);

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ProjectRepository projectRepository;
    private final CategoryRepository categoryRepository;

    private final JdbcTemplate jdbcTemplate;

    /**
     * 서버가 시작된 후 자동으로 한 번 호출되는 메서드
     * @param args 실행 시 전달할 명령어
     */
    @Override
    public void run(String... args) {
        log.info("===== 성능 테스트 데이터 준비 시작 =====");

        Member member = createMember();
        List<Project> projects = createProjects(member);
        createCategories(projects);
        createTasks(projects);
        connectTaskCategories(projects);

        log.info("===== 성능 테스트 데이터 준비 완료 =====");
    }

    /**
     * 성능 테스트 회원이 없으면 생성 있으면 반환
     * @return 성능 테스트 회원
     */
    private Member createMember() {
        return memberRepository.findByEmail(TEST_EMAIL)
                .orElseGet(() -> {
                    Member member = Member.builder()
                            .email(TEST_EMAIL)
                            .password(passwordEncoder.encode(TEST_PASSWORD))
                            .nickname(TEST_NICKNAME)
                            .role(Role.ROLE_USER)
                            .build();

                    return memberRepository.save(member);
                });
    }

    /**
     * 성능 테스트용 프로젝트 5개를 준비
     * @param member 성능 테스트 회원
     * @return 성능 테스트 프로젝트 목록
     */
    private List<Project> createProjects(Member member) {
        return List.of(
                createProject(member, "성능 테스트 프로젝트 1"),
                createProject(member, "성능 테스트 프로젝트 2"),
                createProject(member, "성능 테스트 프로젝트 3"),
                createProject(member, "성능 테스트 프로젝트 4"),
                createProject(member, "성능 테스트 프로젝트 5")
        );
    }

    /**
     * 지정한 이름의 성능 테스트 프로젝트 준비
     * 동일한 이름의 프로젝트가 있으면 기존 프로젝트 반환, 없으면 생성
     * @param member 성능 테스트 회원
     * @param name 생성하거나 조회할 프로젝트 이름
     * @return 생성되었거나 기존에 존재하는 프로젝트
     */
    private Project createProject(Member member, String name) {
        return projectRepository.findByMemberIdAndName(member.getId(), name)
                .orElseGet(() -> {
                    Project project = Project.builder()
                            .member(member)
                            .name(name)
                            .description("Task 목록 조회 성능 측정을 위한 프로젝트")
                            .build();

                    return projectRepository.save(project);
                });
    }

    /**
     * 각 성능 테스트 프로젝트에 공통 카테고리를 준비
     * 동일한 이름의 카테고리가 이미 있으면 새로 만들지 않는다
     * @param projects 카테고리를 생성할 프로젝트 목록
     */
    private void createCategories(List<Project> projects) {
        for (Project project : projects) {
            CATEGORY_NAMES.forEach(name -> createCategory(project, name));
        }
    }

    /**
     * 프로젝트에서 지정한 이름의 카테고리를 준비
     * 이미 존재하면 기존 카테고리를 반환하고, 존재하지 않으면 새로 생성
     * @param project 카테고리가 속할 프로젝트
     * @param name 카테고리 이름
     * @return 생성되었거나 기존에 존재하는 카테고리
     */
    private Category createCategory(Project project, String name) {
        return categoryRepository
                .findByProjectIdAndName(project.getId(), name)
                .orElseGet(() -> {
                    Category category = Category.builder()
                            .project(project)
                            .name(name)
                            .build();

                    return categoryRepository.save(category);
                });
    }

    /**
     * 성능 측정에 사용할 Task 100,000건을 생성한다.
     * @param projects Task가 속할 성능 테스트 프로젝트 목록
     */
    private void createTasks(List<Project> projects) {
        
        // 기존 Task 개수 확인
        long existingTaskCount = countExistingTasks(projects);

        // 이전 실행에서 10만 건이 정상적으로 생성되었다면 그대로 재사용
        if (existingTaskCount == TOTAL_TASK_COUNT) {
            log.info("기존 성능 테스트 Task 재사용: count={}", existingTaskCount);
            return;
        }

        // 일부만 생성된 상태라면 중복을 막기 위해 기존 데이터를 정리한 뒤 다시 만든다
        if (existingTaskCount > 0) {
            log.warn(
                    "성능 테스트 Task 개수가 예상과 달라 재생성합니다: existing={}, expected={}",
                    existingTaskCount,
                    TOTAL_TASK_COUNT
            );
        }
        deleteExistingTasks(projects);
        log.info("Task 생성 시작: count={}", TOTAL_TASK_COUNT);

        // 실행할 INSERT SQL 정의
        String sql = """
            INSERT INTO task (
                project_id,
                title,
                goal,
                work_process,
                lesson_learned,
                started_at,
                finished_at,
                due_date,
                status,
                difficulty,
                priority,
                created_at,
                updated_at
            ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;

        // Task 번호 10만개 생성
        List<Integer> taskIndexes = IntStream.range(0, TOTAL_TASK_COUNT)
                .boxed()
                .toList();

        // Task를 1000개씩 나눠서 저장 (1000개 단위로 묶어서 저장 -> 100개의 배치)
        jdbcTemplate.batchUpdate(
                sql,                                // 반복 실행할 SQL
                taskIndexes,                        // 0부터 99999까지의 Task 순번
                BATCH_SIZE,                         // 한 번에 처리할 개수
                // 각 ? 자리에 값을 넣는 부분
                (preparedStatement, index) -> {
                    Long projectId = selectProjectId(projects, index);
                    TaskStatus status = selectStatus(index);
                    TaskDifficulty difficulty = selectDifficulty(index);
                    TaskPriority priority = selectPriority(index);

                    LocalDateTime createdAt = BASE_DATE
                            .minusDays(index % 730)             // 기준일로부터 최대 729일 전까지 분산
                            .minusSeconds(index % 86400);       // 시간은 하루 범위 안에서 분산

                    LocalDateTime startedAt =
                            createdAt.plusDays(index % 30);     // 생성일로부터 0~29일 후를 시작일로 설정

                    LocalDateTime dueDate = index % 10 == 0     // 순번이 10의 배수면 마감일 X
                            ? null
                            : BASE_DATE.plusDays((index % 365) - 182);      // 기준일 중심으로 과거와 미래에 분산

                    LocalDateTime finishedAt =
                            status == TaskStatus.COMPLETED      // 완료 상태인 Task에만 완료일 넣기
                                    ? startedAt.plusDays((index % 30) + 1)
                                    : null;

                    preparedStatement.setLong(1, projectId);
                    preparedStatement.setString(2, "성능 테스트 작업 %06d".formatted(index + 1));
                    preparedStatement.setString(3, "성능 테스트 목표");
                    preparedStatement.setString(4, "성능 테스트 작업 과정");
                    preparedStatement.setString(5, "성능 테스트 회고");
                    preparedStatement.setTimestamp(6, Timestamp.valueOf(startedAt));

                    // 날짜 값이 7번째 ? 자리에 NULL 넣기, 있다면 JDBC가 처리 가능한 Timestamp로 변환해서 넣기
                    if (finishedAt == null) {
                        preparedStatement.setNull(7, Types.TIMESTAMP);
                    } else {
                        preparedStatement.setTimestamp(7, Timestamp.valueOf(finishedAt));
                    }

                    if (dueDate == null) {
                        preparedStatement.setNull(8, Types.TIMESTAMP);
                    } else {
                        preparedStatement.setTimestamp(8, Timestamp.valueOf(dueDate));
                    }

                    preparedStatement.setString(9, status.name());
                    preparedStatement.setString(10, difficulty.name());
                    preparedStatement.setString(11, priority.name());
                    preparedStatement.setTimestamp(12, Timestamp.valueOf(createdAt));
                    preparedStatement.setTimestamp(13, Timestamp.valueOf(createdAt));
                }
        );

        log.info("Task 생성 완료: count={}", TOTAL_TASK_COUNT);
    }

    /**
     * 성능 테스트용 프로젝트 5개에 저장된 전체 Task 수를 조회
     * @param projects 성능 테스트 프로젝트 목록
     * @return 현재 저장된 Task 수
     */
    private long countExistingTasks(List<Project> projects) {
        String placeholders = createPlaceholders(projects.size());
        Object[] projectIds = extractProjectIds(projects);

        Long count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM task WHERE project_id IN (%s)"
                        .formatted(placeholders),
                Long.class,
                projectIds
        );

        return count == null ? 0L : count;
    }

    /**
     * 기존 성능 테스트 프로젝트의 Task를 삭제한다.
     */
    private void deleteExistingTasks(List<Project> projects) {
        String placeholders = createPlaceholders(projects.size());
        Object[] projectIds = extractProjectIds(projects);

        jdbcTemplate.update(
                """
                DELETE FROM category_task
                WHERE task_id IN (
                    SELECT task_id
                    FROM task
                    WHERE project_id IN (%s)
                )
                """.formatted(placeholders),
                projectIds
        );

        int deletedCount = jdbcTemplate.update(
                "DELETE FROM task WHERE project_id IN (%s)"
                        .formatted(placeholders),
                projectIds
        );

        log.info("기존 성능 테스트 Task 삭제 완료: count={}", deletedCount);
    }

    /**
     * SQL의 IN 절에 사용할 물음표 자리표시자를 만든다.
     */
    private String createPlaceholders(int count) {
        return String.join(", ", java.util.Collections.nCopies(count, "?"));
    }

    /**
     * 프로젝트 목록에서 JDBC 쿼리에 전달할 ID 배열을 만든다
     */
    private Object[] extractProjectIds(List<Project> projects) {
        return projects.stream()
                .map(Project::getId)
                .toArray();
    }

    /**
     * Task 번호에 따라 Task가 속할 프로젝트를 결정한다
     * 프로젝트별 데이터 분포는 50%, 20%, 15%, 10%, 5%이다
     * @param projects 성능 테스트 프로젝트 목록
     * @param index 0부터 시작하는 Task 순번
     * @return 선택된 프로젝트 ID
     */
    private Long selectProjectId(
            List<Project> projects,
            int index
    ) {
        if (index < 50_000) {
            return projects.get(0).getId();
        }

        if (index < 70_000) {
            return projects.get(1).getId();
        }

        if (index < 85_000) {
            return projects.get(2).getId();
        }

        if (index < 95_000) {
            return projects.get(3).getId();
        }

        return projects.get(4).getId();
    }

    /**
     * Task 순번을 이용해 상태를 일정한 비율로 배분한다.
     */
    private TaskStatus selectStatus(int index) {
        int value = index % 100;

        if (value < 30) {
            return TaskStatus.TODO;
        }

        if (value < 55) {
            return TaskStatus.IN_PROGRESS;
        }

        if (value < 65) {
            return TaskStatus.ON_HOLD;
        }

        if (value < 95) {
            return TaskStatus.COMPLETED;
        }

        return TaskStatus.CANCELED;
    }

    /**
     * Task 순번을 이용해 난이도를 EASY 30%, NORMAL 50%, HARD 20%로 배분한다
     */
    private TaskDifficulty selectDifficulty(int index) {
        int value = index % 100;

        if (value < 30) {
            return TaskDifficulty.EASY;
        }

        if (value < 80) {
            return TaskDifficulty.NORMAL;
        }

        return TaskDifficulty.HARD;
    }

    /**
     * Task 순번을 이용해 우선순위를 LOW 30%, MEDIUM 50%, HIGH 20%로 배분한다
     */
    private TaskPriority selectPriority(int index) {
        int value = index % 100;

        if (value < 30) {
            return TaskPriority.LOW;
        }

        if (value < 80) {
            return TaskPriority.MEDIUM;
        }

        return TaskPriority.HIGH;
    }

    /**
     * 각 Task를 같은 프로젝트에 속한 카테고리 하나와 연결
     * 이미 모든 Task가 카테고리와 연결되어 있으면 기존 데이터를 재사용
     * @param projects 성능 테스트 프로젝트 목록
     */
    private void connectTaskCategories(List<Project> projects) {
        String placeholders = createPlaceholders(projects.size());
        Object[] projectIds = extractProjectIds(projects);

        Long taskCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM task
                WHERE project_id IN (%s)
                """.formatted(placeholders),
                Long.class,
                projectIds
        );

        Long connectionCount = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM category_task ct
                JOIN task t ON t.task_id = ct.task_id
                WHERE t.project_id IN (%s)
                """.formatted(placeholders),
                Long.class,
                projectIds
        );

        // Task마다 카테고리가 하나씩 연결되어 있다면 다시 만들지 않음
        if (taskCount != null && taskCount.equals(connectionCount)) {
            log.info("기존 Task-Category 연결 재사용: count={}", connectionCount);
            return;
        }

        // 일부만 연결된 상태라면 기존 연결을 삭제하고 다시 생성
        jdbcTemplate.update(
                """
                DELETE FROM category_task
                WHERE task_id IN (
                    SELECT task_id
                    FROM task
                    WHERE project_id IN (%s)
                )
                """.formatted(placeholders),
                projectIds
        );

        int insertedCount = jdbcTemplate.update(
                """
                INSERT INTO category_task (task_id, category_id)
                SELECT
                    t.task_id,
                    c.category_id
                FROM task t
                JOIN category c
                    ON c.project_id = t.project_id
                    AND c.name = CASE MOD(t.task_id, 8)
                        WHEN 0 THEN '백엔드'
                        WHEN 1 THEN '프론트엔드'
                        WHEN 2 THEN '디자인'
                        WHEN 3 THEN '기획'
                        WHEN 4 THEN '리서치'
                        WHEN 5 THEN '테스트'
                        WHEN 6 THEN '문서화'
                        WHEN 7 THEN '유지보수'
                    END
                WHERE t.project_id IN (%s)
                """.formatted(placeholders),
                projectIds
        );

        log.info("Task-Category 연결 생성 완료: count={}", insertedCount);
    }
}