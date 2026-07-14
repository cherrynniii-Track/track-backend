package com.track.track.service.dashboard;

import com.track.track.dto.dashboard.DashboardResponse;
import com.track.track.enums.task.TaskDifficulty;
import com.track.track.enums.task.TaskStatus;
import com.track.track.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/**
 * 캐시 확인 및 실제 통계 집계
 */
@Service
@RequiredArgsConstructor
public class DashboardCacheService {

    private final TaskRepository taskRepository;

    /**
     * 프로젝트의 대시보드 통계를 조회한다
     * 캐시가 존재하면 캐시된 결과를 반환하고, 존재하지 않으면 DB 집계 결과를 캐시에 저장한다
     * @param projectId 조회할 프로젝트 ID
     * @return 대시보드 통계 결과
     */
    @Cacheable(
            value = "dashboard",
            key = "#projectId",
            sync = true
    )
    public DashboardResponse getDashboardStatistics(Long projectId) {
        Map<TaskStatus, Long> statusCounts = createStatusCounts(projectId);
        Map<TaskDifficulty, Long> difficultyCounts = createDifficultyCounts(projectId);

        long totalTaskCount = taskRepository.countByProjectId(projectId);
        long overdueTaskCount =
                taskRepository.countOverdueTasks(
                        projectId,
                        LocalDateTime.now(),
                        TaskStatus.COMPLETED,
                        TaskStatus.CANCELED
                );

        return DashboardResponse.builder()
                .totalTaskCount(totalTaskCount)
                .statusCounts(statusCounts)
                .difficultyCounts(difficultyCounts)
                .overdueTaskCount(overdueTaskCount)
                .build();
    }

    /**
     * 프로젝트의 상태별 작업 수 조회
     * @param projectId 조회할 프로젝트 ID
     * @return 상태별 작업 수
     */
    private Map<TaskStatus, Long> createStatusCounts(Long projectId) {
        Map<TaskStatus, Long> counts = new EnumMap<>(TaskStatus.class);

        Arrays.stream(TaskStatus.values())
                .forEach(status -> counts.put(status, 0L));

        taskRepository.countByStatusGroup(projectId)
                .forEach(result ->
                        counts.put(
                                result.getStatus(),
                                result.getCount()
                        )
                );

        return counts;
    }

    /**
     * 프로젝트의 난이도별 작업 수를 조회
     * @param projectId 조회할 프로젝트 ID
     * @return 난이도별 작업 수
     */
    private Map<TaskDifficulty, Long> createDifficultyCounts(Long projectId) {
        Map<TaskDifficulty, Long> counts = new EnumMap<>(TaskDifficulty.class);

        Arrays.stream(TaskDifficulty.values())
                .forEach(difficulty -> counts.put(difficulty, 0L));

        taskRepository.countByDifficultyGroup(projectId)
                .forEach(result ->
                        counts.put(
                                result.getDifficulty(),
                                result.getCount()
                        )
                );

        return counts;
    }
}