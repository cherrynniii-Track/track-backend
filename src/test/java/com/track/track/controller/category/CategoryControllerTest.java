package com.track.track.controller.category;

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

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * CategoryController 통합 테스트
 */
class CategoryControllerTest extends AbstractIntegrationTest {

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

    /**
     * 테스트용 카테고리 생성 후 categoryId 반환
     */
    private Long createCategory(String accessToken, Long projectId, String name) throws Exception {
        Map<String, String> request = Map.of(
                "name", name
        );

        String responseBody = mockMvc.perform(post("/api/projects/{projectId}/categories", projectId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("id").asLong();
    }

    @Test
    @DisplayName("카테고리 생성 성공")
    void createCategory_success() throws Exception {
        String accessToken = authTestHelper.signupAndLogin("category-create@test.com");
        Long projectId = projectTestHelper.createProject(accessToken, "Track", "개인 작업 트래커");

        Map<String, String> request = Map.of(
                "name", "백엔드"
        );

        mockMvc.perform(post("/api/projects/{projectId}/categories", projectId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("백엔드"));
    }

    @Test
    @DisplayName("카테고리 목록 조회 성공")
    void getCategories_success() throws Exception {
        String accessToken = authTestHelper.signupAndLogin("category-list@test.com");
        Long projectId = projectTestHelper.createProject(accessToken, "Track", "개인 작업 트래커");

        createCategory(accessToken, projectId, "백엔드");
        createCategory(accessToken, projectId, "프론트엔드");

        mockMvc.perform(get("/api/projects/{projectId}/categories", projectId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("백엔드"))
                .andExpect(jsonPath("$[1].name").value("프론트엔드"));
    }

    @Test
    @DisplayName("카테고리 단건 조회 성공")
    void getCategory_success() throws Exception {
        String accessToken = authTestHelper.signupAndLogin("category-get@test.com");
        Long projectId = projectTestHelper.createProject(accessToken, "Track", "개인 작업 트래커");
        Long categoryId = createCategory(accessToken, projectId, "백엔드");

        mockMvc.perform(get("/api/projects/{projectId}/categories/{categoryId}", projectId, categoryId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(categoryId))
                .andExpect(jsonPath("$.name").value("백엔드"));
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void updateCategory_success() throws Exception {
        String accessToken = authTestHelper.signupAndLogin("category-update@test.com");
        Long projectId = projectTestHelper.createProject(accessToken, "Track", "개인 작업 트래커");
        Long categoryId = createCategory(accessToken, projectId, "백엔드");

        Map<String, String> request = Map.of(
                "name", "DB"
        );

        mockMvc.perform(patch("/api/projects/{projectId}/categories/{categoryId}", projectId, categoryId)
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void deleteCategory_success() throws Exception {
        String accessToken = authTestHelper.signupAndLogin("category-delete@test.com");
        Long projectId = projectTestHelper.createProject(accessToken, "Track", "개인 작업 트래커");
        Long categoryId = createCategory(accessToken, projectId, "백엔드");

        mockMvc.perform(delete("/api/projects/{projectId}/categories/{categoryId}", projectId, categoryId)
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isNoContent());
    }
}