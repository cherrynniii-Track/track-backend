package com.track.track.dto.task;

import com.track.track.domain.Task;
import com.track.track.enums.TaskDifficulty;
import com.track.track.enums.TaskPriority;
import com.track.track.enums.TaskStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class TaskResponse {

    private Long id;
    private Long projectId;

    private String title;
    private String goal;
    private String workProcess;
    private String lessonLearned;

    private LocalDateTime startedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime dueDate;

    private TaskStatus status;
    private TaskDifficulty difficulty;
    private TaskPriority priority;

    private List<TaskCategoryResponse> categories;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskResponse from(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .projectId(task.getProject().getId())
                .title(task.getTitle())
                .goal(task.getGoal())
                .workProcess(task.getWorkProcess())
                .lessonLearned(task.getLessonLearned())
                .startedAt(task.getStartedAt())
                .finishedAt(task.getFinishedAt())
                .dueDate(task.getDueDate())
                .status(task.getStatus())
                .difficulty(task.getDifficulty())
                .priority(task.getPriority())
                .categories(
                        task.getCategories().stream()
                                .map(TaskCategoryResponse::from)
                                .toList()
                )
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}