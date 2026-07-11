package com.track.track.controller.task;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.track.track.AbstractIntegrationTest;
import com.track.track.support.AuthTestHelper;
import com.track.track.support.ProjectTestHelper;
import com.track.track.support.TaskTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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

    @BeforeEach
    void setUp() {
        authTestHelper = new AuthTestHelper(mockMvc, objectMapper);
        projectTestHelper = new ProjectTestHelper(mockMvc, objectMapper);
        taskTestHelper = new TaskTestHelper(mockMvc, objectMapper);
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
    @DisplayName("프로젝트 작업 목록 조회 성공")
    void getTasks_success() throws Exception {
        String accessToken =
                authTestHelper.signupAndLogin("task-list@test.com");

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

        mockMvc.perform(get("/api/projects/{projectId}/tasks", projectId)
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
}