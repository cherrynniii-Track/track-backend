package com.track.track.service.dashboard;

import com.track.track.domain.Project;
import com.track.track.dto.dashboard.DashboardResponse;
import com.track.track.enums.task.TaskDifficulty;
import com.track.track.enums.task.TaskStatus;
import com.track.track.repository.TaskRepository;
import com.track.track.service.support.ProjectSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final TaskRepository taskRepository;
    private final ProjectSupport projectSupport;

    /**
     * 특정 프로젝트의 통계 값 가져오기
     * @param memberId 조회하는 회원 ID
     * @param projectId 조회할 프로젝트
     * @return 대시보드 통계 결과 응답
     */
    public DashboardResponse getDashboard(Long memberId, Long projectId) {
        Project project = projectSupport.getProjectById(projectId);
        projectSupport.validateOwner(memberId, project);

        Map<TaskStatus, Long> statusCounts = createStatusCounts(projectId);
        Map<TaskDifficulty, Long> difficultyCounts = createDifficultyCounts(projectId);

        long totalTaskCount = taskRepository.countByProjectId(projectId);
        long overdueTaskCount = taskRepository.countOverdueTasks(
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
     * 특정 프로젝트의 상태별 작업 수를 조회한다
     * 작업이 존재하지 않는 상태도 0으로 포함한다
     * @param projectId 프로젝트 ID
     * @return 상태별 작업 수
     */
    private Map<TaskStatus, Long> createStatusCounts(Long projectId) {
        Map<TaskStatus, Long> statusCounts = new EnumMap<>(TaskStatus.class);
        Arrays.stream(TaskStatus.values())
                .forEach(status -> statusCounts.put(status, 0L));

        taskRepository.countByStatusGroup(projectId)
                .forEach(result ->
                        statusCounts.put(
                                result.getStatus(),
                                result.getCount()
                        )
                );

        return statusCounts;
    }

    /**
     * 특정 프로젝트의 난이도별 작업 수를 조회한다.
     * 작업이 존재하지 않는 난이도도 0으로 포함한다.
     * @param projectId 프로젝트 ID
     * @return 난이도별 작업 수
     */
    private Map<TaskDifficulty, Long> createDifficultyCounts(Long projectId) {
        Map<TaskDifficulty, Long> difficultyCounts = new EnumMap<>(TaskDifficulty.class);
        Arrays.stream(TaskDifficulty.values())
                .forEach(difficulty ->
                        difficultyCounts.put(difficulty, 0L)
                );

        taskRepository.countByDifficultyGroup(projectId)
                .forEach(result ->
                        difficultyCounts.put(
                                result.getDifficulty(),
                                result.getCount()
                        )
                );

        return difficultyCounts;
    }
}