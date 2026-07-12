package com.track.track.controller.dashboard;

import com.track.track.dto.dashboard.DashboardResponse;
import com.track.track.service.auth.CustomUserDetails;
import com.track.track.service.dashboard.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/projects/{projectId}/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    /**
     * 특정 프로젝트의 대시보드 통계를 조회한다.
     * @param userDetails 인증된 사용자 정보
     * @param projectId 통계를 조회할 프로젝트 ID
     * @return 프로젝트 대시보드 통계
     */
    @GetMapping
    public ResponseEntity<DashboardResponse> getDashboard(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long projectId
    ) {
        DashboardResponse response = dashboardService.getDashboard(
                userDetails.getMemberId(),
                projectId
        );

        return ResponseEntity.ok(response);
    }
}