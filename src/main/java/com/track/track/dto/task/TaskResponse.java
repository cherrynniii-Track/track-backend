package com.track.track.dto.task;

import com.track.track.domain.Task;
import com.track.track.enums.task.TaskDifficulty;
import com.track.track.enums.task.TaskPriority;
import com.track.track.enums.task.TaskStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
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

    public TaskResponse(Task task) {
        this.id = task.getId();
        this.projectId = task.getProject().getId();
        this.title = task.getTitle();
        this.goal = task.getGoal();
        this.workProcess = task.getWorkProcess();
        this.lessonLearned = task.getLessonLearned();
        this.startedAt = task.getStartedAt();
        this.finishedAt = task.getFinishedAt();
        this.dueDate = task.getDueDate();
        this.status = task.getStatus();
        this.difficulty = task.getDifficulty();
        this.priority = task.getPriority();
        this.categories = task.getCategories().stream()
                .map(TaskCategoryResponse::from)
                .toList();
        this.createdAt = task.getCreatedAt();
        this.updatedAt = task.getUpdatedAt();
    }
}