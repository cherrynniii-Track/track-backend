package com.track.track.dto.task;

import com.track.track.enums.task.TaskDifficulty;
import com.track.track.enums.task.TaskPriority;
import com.track.track.enums.task.TaskStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class TaskSearchCondition {

    private TaskStatus status;
    private TaskDifficulty difficulty;
    private TaskPriority priority;
    private Long categoryId;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dueDateFrom;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dueDateTo;
}