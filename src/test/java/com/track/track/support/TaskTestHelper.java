package com.track.track.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.track.track.enums.task.TaskDifficulty;
import com.track.track.enums.task.TaskPriority;
import com.track.track.enums.task.TaskStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TaskTestHelper {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public TaskTestHelper(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    /**
     * 테스트용 작업 생성 후 taskId 반환
     */
    public Long createTask(
            String accessToken,
            Long projectId,
            String title
    ) throws Exception {
        Map<String, Object> request = Map.of(
                "title", title,
                "categoryIds", List.of()
        );

        String responseBody = mockMvc.perform(
                        post("/api/projects/{projectId}/tasks", projectId)
                                .header(
                                        "Authorization",
                                        "Bearer " + accessToken
                                )
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        objectMapper.writeValueAsString(request)
                                )
                )
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        JsonNode jsonNode = objectMapper.readTree(responseBody);
        return jsonNode.get("id").asLong();
    }

    public Long createTask(
            String accessToken,
            Long projectId,
            String title,
            TaskStatus status,
            TaskDifficulty difficulty,
            TaskPriority priority,
            LocalDateTime dueDate,
            List<Long> categoryIds
    ) throws Exception {

        Map<String, Object> request = new HashMap<>();
        request.put("title", title);
        request.put("status", status);
        request.put("difficulty", difficulty);
        request.put("priority", priority);
        request.put("dueDate", dueDate);
        request.put("categoryIds", categoryIds);

        MvcResult result = mockMvc.perform(
                        post("/api/projects/{projectId}/tasks", projectId)
                                .header("Authorization", "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(
                result.getResponse().getContentAsString()
        ).get("id").asLong();
    }
}