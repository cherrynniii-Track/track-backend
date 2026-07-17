package com.track.track.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 카테고리 테스트 데이터 생성 헬퍼
 */
public class CategoryTestHelper {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public CategoryTestHelper(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    /**
     * 테스트용 카테고리 생성 후 categoryId 반환
     */
    public Long createCategory(String accessToken, Long projectId, String name) throws Exception {
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
}