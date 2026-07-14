package com.track.track.controller.dashboard;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.track.track.AbstractIntegrationTest;
import com.track.track.enums.task.TaskStatus;
import com.track.track.repository.TaskRepository;
import com.track.track.support.AuthTestHelper;
import com.track.track.support.ProjectTestHelper;
import com.track.track.support.TaskTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Dashboard Redis 캐시 통합 테스트
 */
class DashboardCacheIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CacheManager cacheManager;

    @MockitoSpyBean
    private TaskRepository taskRepository;

    private AuthTestHelper authTestHelper;
    private ProjectTestHelper projectTestHelper;
    private TaskTestHelper taskTestHelper;

    @BeforeEach
    void setUp() {
        authTestHelper = new AuthTestHelper(mockMvc, objectMapper);
        projectTestHelper = new ProjectTestHelper(mockMvc, objectMapper);
        taskTestHelper = new TaskTestHelper(mockMvc, objectMapper);

        Cache dashboardCache = cacheManager.getCache("dashboard");

        if (dashboardCache != null) {
            dashboardCache.clear();
        }

        clearInvocations(taskRepository);
    }

    /**
     * 대시보드 처음 조회했을 때 실제로 DB 집계 메서드가 실행되는지 확인
     */
    @Test
    @DisplayName("최초 대시보드 조회 시 DB 집계 쿼리 실행")
    void getDashboard_firstRequest_executesDatabaseQuery() throws Exception {
        String accessToken =
                authTestHelper.signupAndLogin("dashboard-cache-first@test.com");

        Long projectId = projectTestHelper.createProject(
                accessToken,
                "Track",
                "최초 캐시 조회 테스트"
        );

        taskTestHelper.createTask(accessToken, projectId,"첫 번째 작업");

        // Mockito가 기록한 taskRepository의 메서드 호출 횟수 기록 지우기
        clearInvocations(taskRepository);

        // 실제 대시보드 조회
        getDashboard(accessToken, projectId,1);
        verify(taskRepository, times(1))
                .countByProjectId(projectId);
        verify(taskRepository, times(1))
                .countByStatusGroup(projectId);
        verify(taskRepository, times(1))
                .countByDifficultyGroup(projectId);
        verify(taskRepository, times(1))
                .countOverdueTasks(
                        eq(projectId),
                        any(LocalDateTime.class),
                        eq(TaskStatus.COMPLETED),
                        eq(TaskStatus.CANCELED)
                );
    }

    /**
     * 재조회 시 캐시를 사용하는지 테스트
     */
    @Test
    @DisplayName("대시보드 재조회 시 DB 집계 쿼리 없이 캐시 사용")
    void getDashboard_secondRequest_usesCache() throws Exception {
        String accessToken =
                authTestHelper.signupAndLogin("dashboard-cache-second@test.com");

        Long projectId = projectTestHelper.createProject(
                accessToken,
                "Track",
                "재조회 캐시 테스트"
        );

        taskTestHelper.createTask(accessToken, projectId, "캐시 테스트 작업");

        // 최초 조회: DB 집계 후 캐시에 저장
        getDashboard(
                accessToken,
                projectId,
                1
        );

        clearInvocations(taskRepository);

        // 재조회: 캐시에서 반환
        getDashboard(
                accessToken,
                projectId,
                1
        );

        verify(taskRepository, never())
                .countByProjectId(projectId);
        verify(taskRepository, never())
                .countByStatusGroup(projectId);
        verify(taskRepository, never())
                .countByDifficultyGroup(projectId);
        verify(taskRepository, never())
                .countOverdueTasks(
                        eq(projectId),
                        any(LocalDateTime.class),
                        eq(TaskStatus.COMPLETED),
                        eq(TaskStatus.CANCELED)
                );
    }

    /**
     * 작업 생성 시 캐시를 갱신하는지 확인
     */
    @Test
    @DisplayName("작업 생성 후 대시보드 캐시 갱신")
    void getDashboard_afterTaskCreated_refreshesCache() throws Exception {
        String accessToken =
                authTestHelper.signupAndLogin("dashboard-cache-refresh@test.com");

        Long projectId = projectTestHelper.createProject(
                accessToken,
                "Track",
                "작업 생성 후 캐시 갱신 테스트"
        );

        taskTestHelper.createTask(accessToken, projectId, "첫 번째 작업");

        // 작업 수 1개인 통계를 캐시에 저장
        getDashboard(accessToken, projectId, 1);

        // Task 생성 시 기존 대시보드 캐시가 삭제되어야 한다.
        taskTestHelper.createTask(accessToken, projectId, "두 번째 작업");

        clearInvocations(taskRepository);

        // 기존 캐시가 삭제되었으므로 다시 DB 집계를 하는지 확인
        getDashboard(accessToken, projectId, 2);

        verify(taskRepository, times(1))
                .countByProjectId(projectId);
        verify(taskRepository, times(1))
                .countByStatusGroup(projectId);
        verify(taskRepository, times(1))
                .countByDifficultyGroup(projectId);
        verify(taskRepository, times(1))
                .countOverdueTasks(
                        eq(projectId),
                        any(LocalDateTime.class),
                        eq(TaskStatus.COMPLETED),
                        eq(TaskStatus.CANCELED)
                );
    }

    @Test
    @DisplayName("대시보드 캐시는 프로젝트별로 분리")
    void getDashboard_cacheIsSeparatedByProject() throws Exception {
        String accessToken =
                authTestHelper.signupAndLogin("dashboard-cache-project@test.com");

        Long firstProjectId = projectTestHelper.createProject(
                accessToken,
                "첫 번째 프로젝트",
                "프로젝트별 캐시 테스트"
        );

        Long secondProjectId = projectTestHelper.createProject(
                accessToken,
                "두 번째 프로젝트",
                "프로젝트별 캐시 테스트"
        );

        taskTestHelper.createTask(accessToken, firstProjectId,"첫 번째 프로젝트 작업");
        taskTestHelper.createTask(accessToken, secondProjectId, "두 번째 프로젝트 작업 1");
        taskTestHelper.createTask(accessToken, secondProjectId, "두 번째 프로젝트 작업 2");

        // 프로젝트별로 서로 다른 통계를 캐시에 저장
        getDashboard(accessToken, firstProjectId, 1);
        getDashboard(accessToken, secondProjectId, 2);

        clearInvocations(taskRepository);

         // 두 프로젝트를 다시 조회했을 때 각각 자기 프로젝트의 캐시값이 반환되어야 한다.
        getDashboard(accessToken, firstProjectId, 1);
        getDashboard(accessToken, secondProjectId, 2);

        verify(taskRepository, never())
                .countByProjectId(firstProjectId);
        verify(taskRepository, never())
                .countByProjectId(secondProjectId);
    }

    /**
     * 대시보드를 조회하고 전체 작업 수를 검증한다.
     * @param accessToken 인증 토큰
     * @param projectId 조회할 프로젝트 ID
     * @param expectedTaskCount 예상 전체 작업 수
     */
    private void getDashboard(
            String accessToken,
            Long projectId,
            int expectedTaskCount
    ) throws Exception {
        mockMvc.perform(get(
                        "/api/projects/{projectId}/dashboard",
                        projectId
                )
                        .header(
                                "Authorization",
                                "Bearer " + accessToken
                        ))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalTaskCount")
                        .value(expectedTaskCount));
    }
}