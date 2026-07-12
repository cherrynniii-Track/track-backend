package com.track.track.controller.dashboard;

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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * DashboardController 통합 테스트
 */
class DashboardControllerTest extends AbstractIntegrationTest {

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
    @DisplayName("대시보드 통계 조회 성공")
    void getDashboard_success() throws Exception {
        String accessToken =
                authTestHelper.signupAndLogin("dashboard@test.com");

        Long projectId = projectTestHelper.createProject(
                accessToken,
                "Track",
                "대시보드 통계 테스트"
        );

        // 지연 작업
        createTask(
                accessToken,
                projectId,
                "마감이 지난 TODO 작업",
                "TODO",
                "EASY",
                LocalDateTime.now().minusDays(2)
        );

        // 지연 작업
        createTask(
                accessToken,
                projectId,
                "마감이 지난 진행 중 작업",
                "IN_PROGRESS",
                "NORMAL",
                LocalDateTime.now().minusDays(1)
        );

        // 완료된 작업이므로 마감일이 지났어도 지연 작업에서 제외
        createTask(
                accessToken,
                projectId,
                "완료된 작업",
                "COMPLETED",
                "HARD",
                LocalDateTime.now().minusDays(3)
        );

        // 마감일이 지나지 않았으므로 지연 작업에서 제외
        createTask(
                accessToken,
                projectId,
                "진행 예정 작업",
                "TODO",
                "NORMAL",
                LocalDateTime.now().plusDays(3)
        );

        // 취소된 작업이므로 마감일이 지났어도 지연 작업에서 제외
        createTask(
                accessToken,
                projectId,
                "취소된 작업",
                "CANCELED",
                "HARD",
                LocalDateTime.now().minusDays(4)
        );

        mockMvc.perform(get(
                        "/api/projects/{projectId}/dashboard",
                        projectId
                )
                        .header(
                                "Authorization",
                                "Bearer " + accessToken
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTaskCount").value(5))

                .andExpect(jsonPath("$.statusCounts.TODO").value(2))
                .andExpect(jsonPath("$.statusCounts.IN_PROGRESS").value(1))
                .andExpect(jsonPath("$.statusCounts.ON_HOLD").value(0))
                .andExpect(jsonPath("$.statusCounts.COMPLETED").value(1))
                .andExpect(jsonPath("$.statusCounts.CANCELED").value(1))

                .andExpect(jsonPath("$.difficultyCounts.EASY").value(1))
                .andExpect(jsonPath("$.difficultyCounts.NORMAL").value(2))
                .andExpect(jsonPath("$.difficultyCounts.HARD").value(2))

                .andExpect(jsonPath("$.overdueTaskCount").value(2));
    }

    @Test
    @DisplayName("작업이 없는 프로젝트의 대시보드 통계 조회 성공")
    void getDashboard_withoutTask_success() throws Exception {
        String accessToken =
                authTestHelper.signupAndLogin("dashboard-empty@test.com");

        Long projectId = projectTestHelper.createProject(
                accessToken,
                "빈 프로젝트",
                "작업이 없는 대시보드 테스트"
        );

        mockMvc.perform(get(
                        "/api/projects/{projectId}/dashboard",
                        projectId
                )
                        .header(
                                "Authorization",
                                "Bearer " + accessToken
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTaskCount").value(0))

                .andExpect(jsonPath("$.statusCounts.TODO").value(0))
                .andExpect(jsonPath("$.statusCounts.IN_PROGRESS").value(0))
                .andExpect(jsonPath("$.statusCounts.ON_HOLD").value(0))
                .andExpect(jsonPath("$.statusCounts.COMPLETED").value(0))
                .andExpect(jsonPath("$.statusCounts.CANCELED").value(0))

                .andExpect(jsonPath("$.difficultyCounts.EASY").value(0))
                .andExpect(jsonPath("$.difficultyCounts.NORMAL").value(0))
                .andExpect(jsonPath("$.difficultyCounts.HARD").value(0))

                .andExpect(jsonPath("$.overdueTaskCount").value(0));
    }

    @Test
    @DisplayName("다른 사용자의 프로젝트 대시보드 조회 실패")
    void getDashboard_accessDenied() throws Exception {
        String ownerToken =
                authTestHelper.signupAndLogin("dashboard-owner@test.com");

        String otherMemberToken =
                authTestHelper.signupAndLogin("dashboard-other@test.com");

        Long projectId = projectTestHelper.createProject(
                ownerToken,
                "소유자 프로젝트",
                "접근 권한 테스트"
        );

        mockMvc.perform(get(
                        "/api/projects/{projectId}/dashboard",
                        projectId
                )
                        .header(
                                "Authorization",
                                "Bearer " + otherMemberToken
                        ))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("존재하지 않는 프로젝트 대시보드 조회 실패")
    void getDashboard_projectNotFound() throws Exception {
        String accessToken =
                authTestHelper.signupAndLogin("dashboard-not-found@test.com");

        mockMvc.perform(get(
                        "/api/projects/{projectId}/dashboard",
                        999999L
                )
                        .header(
                                "Authorization",
                                "Bearer " + accessToken
                        ))
                .andExpect(status().isNotFound());
    }

    /**
     * 대시보드 통계 테스트에 사용할 작업을 생성한다.
     *
     * @param accessToken 인증 토큰
     * @param projectId 작업이 속할 프로젝트 ID
     * @param title 작업 제목
     * @param status 작업 상태
     * @param difficulty 작업 난이도
     * @param dueDate 작업 마감일
     */
    private void createTask(
            String accessToken,
            Long projectId,
            String title,
            String status,
            String difficulty,
            LocalDateTime dueDate
    ) throws Exception {
        Map<String, Object> request = Map.of(
                "title", title,
                "status", status,
                "difficulty", difficulty,
                "dueDate", dueDate.toString(),
                "categoryIds", List.of()
        );

        mockMvc.perform(post(
                        "/api/projects/{projectId}/tasks",
                        projectId
                )
                        .header(
                                "Authorization",
                                "Bearer " + accessToken
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }
}