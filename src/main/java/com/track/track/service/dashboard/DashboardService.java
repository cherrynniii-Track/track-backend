package com.track.track.service.dashboard;

import com.track.track.domain.Project;
import com.track.track.dto.dashboard.DashboardResponse;
import com.track.track.service.support.ProjectSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 프로젝트 존재 여부와 소유권 검사
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final ProjectSupport projectSupport;
    private final DashboardCacheService dashboardCacheService;

    /**
     * 특정 프로젝트의 소유권을 검증하고 대시보드 통계를 조회한다.
     * @param memberId 조회하는 회원 ID
     * @param projectId 조회할 프로젝트 ID
     * @return 대시보드 통계 결과
     */
    public DashboardResponse getDashboard(Long memberId, Long projectId) {
        Project project = projectSupport.getProjectById(projectId);
        projectSupport.validateOwner(memberId, project);

        return dashboardCacheService.getDashboardStatistics(projectId);
    }
}