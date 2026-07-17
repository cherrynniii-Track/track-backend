package com.track.track.controller.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.track.track.AbstractIntegrationTest;
import com.track.track.enums.task.TaskDifficulty;
import com.track.track.enums.task.TaskPriority;
import com.track.track.enums.task.TaskStatus;
import com.track.track.support.AuthTestHelper;
import com.track.track.support.CategoryTestHelper;
import com.track.track.support.ProjectTestHelper;
import com.track.track.support.TaskTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * TaskController 통합 테스트
 */
class TaskControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthTestHelper authTestHelper;
    private ProjectTestHelper projectTestHelper;
    private TaskTestHelper taskTestHelper;
    private CategoryTestHelper categoryTestHelper;

    @BeforeEach
    void setUp() {
        authTestHelper = new AuthTestHelper(mockMvc, objectMapper);
        projectTestHelper = new ProjectTestHelper(mockMvc, objectMapper);
        taskTestHelper = new TaskTestHelper(mockMvc, objectMapper);
        categoryTestHelper = new CategoryTestHelper(mockMvc, objectMapper);
    }

    @Test
    @DisplayName("카테고리 없이 작업 생성 성공")
    void createTask_withoutCategory_success() throws Exception {
        String accessToken = authTestHelper.signupAndLogin("task-create@test.com");

        Long projectId = projectTestHelper.createProject(
                accessToken,
                "Track",
                "작업 생성 테스트"
        );

        Map<String, Object> request = Map.of(
                "title", "TaskController 작성",
                "categoryIds", List.of()
        );

        mockMvc.perform(post("/api/projects/{projectId}/tasks", projectId)
                        .header(
                                "Authorization",
                                "Bearer " + accessToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title")
                        .value("TaskController 작성"));
    }

    @Test
    @DisplayName("프로젝트 전체 작업 목록 조회 성공")
    void getAllTasks_success() throws Exception {
        String accessToken = authTestHelper.signupAndLogin("task-list@test.com");

        Long projectId = projectTestHelper.createProject(
                accessToken,
                "Track",
                "작업 목록 조회 테스트"
        );

        taskTestHelper.createTask(
                accessToken,
                projectId,
                "첫 번째 작업"
        );

        taskTestHelper.createTask(
                accessToken,
                projectId,
                "두 번째 작업"
        );

        mockMvc.perform(get("/api/projects/{projectId}/tasks/all", projectId)
                        .header(
                                "Authorization",
                                "Bearer " + accessToken
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[*].title").value(
                        containsInAnyOrder(
                                "첫 번째 작업",
                                "두 번째 작업"
                        )
                ));
    }

    @Test
    @DisplayName("작업 단건 조회 성공")
    void getTask_success() throws Exception {
        String accessToken =
                authTestHelper.signupAndLogin("task-get@test.com");

        Long projectId = projectTestHelper.createProject(
                accessToken,
                "Track",
                "작업 단건 조회 테스트"
        );

        Long taskId = taskTestHelper.createTask(
                accessToken,
                projectId,
                "조회할 작업"
        );

        mockMvc.perform(get(
                        "/api/projects/{projectId}/tasks/{taskId}",
                        projectId,
                        taskId
                )
                        .header(
                                "Authorization",
                                "Bearer " + accessToken
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("조회할 작업"));
    }

    @Test
    @DisplayName("작업 수정 성공")
    void updateTask_success() throws Exception {
        String accessToken =
                authTestHelper.signupAndLogin("task-update@test.com");

        Long projectId = projectTestHelper.createProject(
                accessToken,
                "Track",
                "작업 수정 테스트"
        );

        Long taskId = taskTestHelper.createTask(
                accessToken,
                projectId,
                "수정 전 작업"
        );

        Map<String, Object> request = Map.of(
                "title", "수정 후 작업"
        );

        mockMvc.perform(patch(
                        "/api/projects/{projectId}/tasks/{taskId}",
                        projectId,
                        taskId
                )
                        .header(
                                "Authorization",
                                "Bearer " + accessToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(
                        "/api/projects/{projectId}/tasks/{taskId}",
                        projectId,
                        taskId
                )
                        .header(
                                "Authorization",
                                "Bearer " + accessToken
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정 후 작업"));
    }

    @Test
    @DisplayName("작업 삭제 성공")
    void deleteTask_success() throws Exception {
        String accessToken =
                authTestHelper.signupAndLogin("task-delete@test.com");

        Long projectId = projectTestHelper.createProject(
                accessToken,
                "Track",
                "작업 삭제 테스트"
        );

        Long taskId = taskTestHelper.createTask(
                accessToken,
                projectId,
                "삭제할 작업"
        );

        mockMvc.perform(delete(
                        "/api/projects/{projectId}/tasks/{taskId}",
                        projectId,
                        taskId
                )
                        .header(
                                "Authorization",
                                "Bearer " + accessToken
                        ))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(
                        "/api/projects/{projectId}/tasks/{taskId}",
                        projectId,
                        taskId
                )
                        .header(
                                "Authorization",
                                "Bearer " + accessToken
                        ))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("작업 목록을 페이지 단위로 조회한다")
    void getTasks_pagination_success() throws Exception {
        String accessToken = authTestHelper.signupAndLogin("task-page@test.com");

        Long projectId = projectTestHelper.createProject(
                accessToken,
                "Track",
                "작업 페이지네이션 테스트"
        );

        taskTestHelper.createTask(accessToken, projectId, "작업 1");
        taskTestHelper.createTask(accessToken, projectId, "작업 2");
        taskTestHelper.createTask(accessToken, projectId, "작업 3");

        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                        .header(
                                "Authorization",
                                "Bearer " + accessToken
                        )
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));

        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                        .header(
                                "Authorization",
                                "Bearer " + accessToken
                        )
                        .param("page", "1")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.first").value(false))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    @DisplayName("페이지 번호가 음수이면 조회에 실패한다")
    void getTasks_invalidPage_fail() throws Exception {
        String accessToken = authTestHelper.signupAndLogin("task-invalid-page@test.com");

        Long projectId = projectTestHelper.createProject(
                accessToken,
                "Track",
                "잘못된 페이지 번호 테스트"
        );

        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                        .header(
                                "Authorization",
                                "Bearer " + accessToken
                        )
                        .param("page", "-1")
                        .param("size", "20"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("페이지 크기가 1보다 작으면 조회에 실패한다")
    void getTasks_invalidMinimumSize_fail() throws Exception {
        String accessToken = authTestHelper.signupAndLogin("task-invalid-size-min@test.com");

        Long projectId = projectTestHelper.createProject(
                accessToken,
                "Track",
                "잘못된 페이지 크기 테스트"
        );

        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                        .header(
                                "Authorization",
                                "Bearer " + accessToken
                        )
                        .param("page", "0")
                        .param("size", "0"))
                .andExpect(jsonPath("$.code").value("INVALID_PAGE_SIZE"));
    }

    @Test
    @DisplayName("페이지 크기가 최대 크기를 초과하면 조회에 실패한다")
    void getTasks_exceedMaximumSize_fail() throws Exception {
        String accessToken = authTestHelper.signupAndLogin("task-invalid-size-max@test.com");

        Long projectId = projectTestHelper.createProject(
                accessToken,
                "Track",
                "페이지 최대 크기 테스트"
        );

        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                        .header(
                                "Authorization",
                                "Bearer " + accessToken
                        )
                        .param("page", "0")
                        .param("size", "101"))
                .andExpect(jsonPath("$.code").value("INVALID_PAGE_SIZE"));
    }

    @Test
    @DisplayName("상태로 작업 목록을 필터링한다")
    void getTasks_filterByStatus_success() throws Exception {
        String accessToken =
                authTestHelper.signupAndLogin("task-status@test.com");

        Long projectId = projectTestHelper.createProject(
                accessToken,
                "Track",
                "상태 필터 테스트"
        );

        taskTestHelper.createTask(
                accessToken,
                projectId,
                "진행 중 작업",
                TaskStatus.IN_PROGRESS,
                TaskDifficulty.NORMAL,
                TaskPriority.MEDIUM,
                LocalDateTime.of(2026, 7, 10, 12, 0),
                List.of()
        );

        taskTestHelper.createTask(
                accessToken,
                projectId,
                "완료 작업",
                TaskStatus.COMPLETED,
                TaskDifficulty.NORMAL,
                TaskPriority.MEDIUM,
                LocalDateTime.of(2026, 7, 11, 12, 0),
                List.of()
        );

        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                        .header("Authorization", "Bearer " + accessToken)
                        .param("status", TaskStatus.IN_PROGRESS.name())
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title")
                        .value("진행 중 작업"))
                .andExpect(jsonPath("$.content[0].status")
                        .value(TaskStatus.IN_PROGRESS.name()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("난이도로 작업 목록을 필터링한다")
    void getTasks_filterByDifficulty_success() throws Exception {
        String accessToken =
                authTestHelper.signupAndLogin("task-difficulty@test.com");

        Long projectId = projectTestHelper.createProject(
                accessToken,
                "Track",
                "난이도 필터 테스트"
        );

        taskTestHelper.createTask(
                accessToken,
                projectId,
                "쉬운 작업",
                TaskStatus.TODO,
                TaskDifficulty.EASY,
                TaskPriority.MEDIUM,
                LocalDateTime.of(2026, 7, 10, 12, 0),
                List.of()
        );

        taskTestHelper.createTask(
                accessToken,
                projectId,
                "어려운 작업",
                TaskStatus.TODO,
                TaskDifficulty.HARD,
                TaskPriority.MEDIUM,
                LocalDateTime.of(2026, 7, 11, 12, 0),
                List.of()
        );

        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                        .header("Authorization", "Bearer " + accessToken)
                        .param("difficulty", TaskDifficulty.HARD.name())
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title")
                        .value("어려운 작업"))
                .andExpect(jsonPath("$.content[0].difficulty")
                        .value(TaskDifficulty.HARD.name()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("우선순위로 작업 목록을 필터링한다")
    void getTasks_filterByPriority_success() throws Exception {
        String accessToken =
                authTestHelper.signupAndLogin("task-priority@test.com");

        Long projectId = projectTestHelper.createProject(
                accessToken,
                "Track",
                "우선순위 필터 테스트"
        );

        taskTestHelper.createTask(
                accessToken,
                projectId,
                "낮은 우선순위 작업",
                TaskStatus.TODO,
                TaskDifficulty.NORMAL,
                TaskPriority.LOW,
                LocalDateTime.of(2026, 7, 10, 12, 0),
                List.of()
        );

        taskTestHelper.createTask(
                accessToken,
                projectId,
                "높은 우선순위 작업",
                TaskStatus.TODO,
                TaskDifficulty.NORMAL,
                TaskPriority.HIGH,
                LocalDateTime.of(2026, 7, 11, 12, 0),
                List.of()
        );

        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                        .header("Authorization", "Bearer " + accessToken)
                        .param("priority", TaskPriority.HIGH.name())
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title")
                        .value("높은 우선순위 작업"))
                .andExpect(jsonPath("$.content[0].priority")
                        .value(TaskPriority.HIGH.name()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("상태와 난이도를 함께 적용해 작업 목록을 필터링한다")
    void getTasks_filterByStatusAndDifficulty_success() throws Exception {
        String accessToken =
                authTestHelper.signupAndLogin("task-combination@test.com");

        Long projectId = projectTestHelper.createProject(
                accessToken,
                "Track",
                "복합 필터 테스트"
        );

        taskTestHelper.createTask(
                accessToken,
                projectId,
                "조회 대상 작업",
                TaskStatus.IN_PROGRESS,
                TaskDifficulty.HARD,
                TaskPriority.HIGH,
                LocalDateTime.of(2026, 7, 10, 12, 0),
                List.of()
        );

        taskTestHelper.createTask(
                accessToken,
                projectId,
                "상태만 일치하는 작업",
                TaskStatus.IN_PROGRESS,
                TaskDifficulty.EASY,
                TaskPriority.HIGH,
                LocalDateTime.of(2026, 7, 11, 12, 0),
                List.of()
        );

        taskTestHelper.createTask(
                accessToken,
                projectId,
                "난이도만 일치하는 작업",
                TaskStatus.TODO,
                TaskDifficulty.HARD,
                TaskPriority.HIGH,
                LocalDateTime.of(2026, 7, 12, 12, 0),
                List.of()
        );

        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                        .header("Authorization", "Bearer " + accessToken)
                        .param("status", TaskStatus.IN_PROGRESS.name())
                        .param("difficulty", TaskDifficulty.HARD.name())
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title")
                        .value("조회 대상 작업"))
                .andExpect(jsonPath("$.content[0].status")
                        .value(TaskStatus.IN_PROGRESS.name()))
                .andExpect(jsonPath("$.content[0].difficulty")
                        .value(TaskDifficulty.HARD.name()))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("마감일 범위로 작업 목록을 필터링한다")
    void getTasks_filterByDueDateRange_success() throws Exception {
        String accessToken =
                authTestHelper.signupAndLogin("task-due-date@test.com");

        Long projectId = projectTestHelper.createProject(
                accessToken,
                "Track",
                "마감일 범위 필터 테스트"
        );

        taskTestHelper.createTask(
                accessToken,
                projectId,
                "범위 이전 작업",
                TaskStatus.TODO,
                TaskDifficulty.NORMAL,
                TaskPriority.MEDIUM,
                LocalDateTime.of(2026, 6, 30, 12, 0),
                List.of()
        );

        taskTestHelper.createTask(
                accessToken,
                projectId,
                "범위 안 작업",
                TaskStatus.TODO,
                TaskDifficulty.NORMAL,
                TaskPriority.MEDIUM,
                LocalDateTime.of(2026, 7, 15, 12, 0),
                List.of()
        );

        taskTestHelper.createTask(
                accessToken,
                projectId,
                "범위 이후 작업",
                TaskStatus.TODO,
                TaskDifficulty.NORMAL,
                TaskPriority.MEDIUM,
                LocalDateTime.of(2026, 8, 1, 12, 0),
                List.of()
        );

        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                        .header("Authorization", "Bearer " + accessToken)
                        .param(
                                "dueDateFrom",
                                LocalDateTime.of(
                                        2026,
                                        7,
                                        1,
                                        0,
                                        0
                                ).toString()
                        )
                        .param(
                                "dueDateTo",
                                LocalDateTime.of(
                                        2026,
                                        7,
                                        31,
                                        23,
                                        59,
                                        59
                                ).toString()
                        )
                        .param("page", "0")
                        .param("size", "20"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title")
                        .value("범위 안 작업"))
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("필터링된 작업 목록에 페이지네이션을 적용한다")
    void getTasks_filterWithPagination_success() throws Exception {
        String accessToken =
                authTestHelper.signupAndLogin(
                        "task-filter-pagination@test.com"
                );

        Long projectId = projectTestHelper.createProject(
                accessToken,
                "Track",
                "필터 페이지네이션 테스트"
        );

        taskTestHelper.createTask(
                accessToken,
                projectId,
                "진행 작업 1",
                TaskStatus.IN_PROGRESS,
                TaskDifficulty.NORMAL,
                TaskPriority.MEDIUM,
                LocalDateTime.of(2026, 7, 10, 12, 0),
                List.of()
        );

        taskTestHelper.createTask(
                accessToken,
                projectId,
                "진행 작업 2",
                TaskStatus.IN_PROGRESS,
                TaskDifficulty.NORMAL,
                TaskPriority.MEDIUM,
                LocalDateTime.of(2026, 7, 11, 12, 0),
                List.of()
        );

        taskTestHelper.createTask(
                accessToken,
                projectId,
                "진행 작업 3",
                TaskStatus.IN_PROGRESS,
                TaskDifficulty.NORMAL,
                TaskPriority.MEDIUM,
                LocalDateTime.of(2026, 7, 12, 12, 0),
                List.of()
        );

        taskTestHelper.createTask(
                accessToken,
                projectId,
                "완료 작업",
                TaskStatus.COMPLETED,
                TaskDifficulty.NORMAL,
                TaskPriority.MEDIUM,
                LocalDateTime.of(2026, 7, 13, 12, 0),
                List.of()
        );

        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                        .header("Authorization", "Bearer " + accessToken)
                        .param("status", TaskStatus.IN_PROGRESS.name())
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.first").value(true))
                .andExpect(jsonPath("$.last").value(false));
    }

    @Test
    @DisplayName("페이지네이션 조회 시 Task별 카테고리 조회 쿼리를 확인한다")
    void getTasks_categoryNPlusOne() throws Exception {
        String accessToken =
                authTestHelper.signupAndLogin("task-n-plus-one@test.com");

        Long projectId = projectTestHelper.createProject(
                accessToken,
                "Track",
                "N+1 재현 테스트"
        );

        Long categoryId = categoryTestHelper.createCategory(
                accessToken,
                projectId,
                "백엔드"
        );

        for (int i = 1; i <= 5; i++) {
            taskTestHelper.createTask(
                    accessToken,
                    projectId,
                    "작업 " + i,
                    TaskStatus.TODO,
                    TaskDifficulty.NORMAL,
                    TaskPriority.MEDIUM,
                    LocalDateTime.of(
                            2026,
                            7,
                            10 + i,
                            12,
                            0
                    ),
                    List.of(categoryId)
            );
        }

        System.out.println("===== N+1 QUERY CHECK START =====");

        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
                        .header(
                                "Authorization",
                                "Bearer " + accessToken
                        )
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalElements").value(5));

        System.out.println("===== N+1 QUERY CHECK END =====");
    }
}