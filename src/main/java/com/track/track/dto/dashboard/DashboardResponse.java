package com.track.track.dto.dashboard;

import com.track.track.enums.task.TaskDifficulty;
import com.track.track.enums.task.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {

    private long totalTaskCount;
    private Map<TaskStatus, Long> statusCounts;
    private Map<TaskDifficulty, Long> difficultyCounts;
    private long overdueTaskCount;
}