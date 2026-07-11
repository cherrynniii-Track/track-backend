package com.track.track.dto.task;

import com.track.track.enums.TaskDifficulty;
import com.track.track.enums.TaskPriority;
import com.track.track.enums.TaskStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class TaskCreateRequest {

    @NotBlank
    @Size(max = 50)
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

    private List<Long> categoryIds = new ArrayList<>();
}
