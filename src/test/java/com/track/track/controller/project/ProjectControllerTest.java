package com.track.track.controller.project;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.track.track.AbstractIntegrationTest;
import com.track.track.support.AuthTestHelper;
import com.track.track.support.ProjectTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ProjectController 통합 테스트
 */
class ProjectControllerTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private AuthTestHelper authTestHelper;
    private ProjectTestHelper projectTestHelper;

    @BeforeEach
    void setUp() {
        authTestHelper = new AuthTestHelper(mockMvc, objectMapper);
        projectTestHelper = new ProjectTestHelper(mockMvc, objectMapper);
    }

    @Test
    @DisplayName("프로젝트 생성 성공")
    void createProject_success() throws Exception {
        String accessToken = authTestHelper.signupAndLogin("project-create@test.com");

        Map<String, String> request = Map.of(
                "name", "Track",
                "description", "개인 작업 트래커"
        );

        mockMvc.perform(post("/api/projects")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Track"))
                .andExpect(jsonPath("$.description").value("개인 작업 트래커"));
    }

    @Test
    @DisplayName("프로젝트 목록 조회 성공")
    void getProjects_success() throws Exception {
        String accessToken = authTestHelper.signupAndLogin("project-list@test.com");

        projectTestHelper.createProject(accessToken, "Track 1", "첫 번째 프로젝트");
        projectTestHelper.createProject(accessToken, "Track 2", "두 번째 프로젝트");

        mockMvc.perform(get("/api/projects")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Track 1"))
                .andExpect(jsonPath("$[1].name").value("Track 2"));
    }

    @Test
    @DisplayName("프로젝트 단건 조회 성공")
    void getProject_success() throws Exception {
        String accessToken = authTestHelper.signupAndLogin("project-get@test.com");

        Long projectId = projectTestHelper.createProject(accessToken, "Track", "조회 테스트");

        mockMvc.perform(get("/api/projects/{projectId}", projectId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(projectId))
                .andExpect(jsonPath("$.name").value("Track"))
                .andExpect(jsonPath("$.description").value("조회 테스트"));
    }

    @Test
    @DisplayName("프로젝트 수정 성공")
    void updateProject_success() throws Exception {
        String accessToken = authTestHelper.signupAndLogin("project-update@test.com");

        Long projectId = projectTestHelper.createProject(accessToken, "수정 전", "수정 전 설명");

        Map<String, String> request = Map.of(
                "name", "수정 후",
                "description", "수정 후 설명"
        );

        mockMvc.perform(patch("/api/projects/{projectId}", projectId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("프로젝트 삭제 성공")
    void deleteProject_success() throws Exception {
        String accessToken = authTestHelper.signupAndLogin("project-delete@test.com");

        Long projectId = projectTestHelper.createProject(accessToken, "삭제할 프로젝트", "삭제 테스트");

        mockMvc.perform(delete("/api/projects/{projectId}", projectId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }
}