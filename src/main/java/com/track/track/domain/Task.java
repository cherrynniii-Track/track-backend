package com.track.track.domain;

import com.track.track.enums.task.TaskDifficulty;
import com.track.track.enums.task.TaskPriority;
import com.track.track.enums.task.TaskStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(
        name = "task",
        indexes = {
                @Index(
                        name = "idx_task_project_due_task",
                        columnList = "project_id, due_date, task_id"
                ),
                @Index(
                        name = "idx_task_project_status_difficulty_due_task",
                        columnList = "project_id, status, difficulty, due_date, task_id"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Task extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "task_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false, length = 50)
    private String title;

    @Lob
    private String goal;

    @Lob
    private String workProcess;

    @Lob
    private String lessonLearned;

    private LocalDateTime startedAt;

    private LocalDateTime finishedAt;

    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskDifficulty difficulty;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskPriority priority;

    @BatchSize(size = 100)      // 최대 페이지 크기가 100이므로 100으로 설정
    @ManyToMany(mappedBy = "tasks")
    private List<Category> categories = new ArrayList<>();

    @Builder
    public Task(
            Project project,
            String title,
            String goal,
            String workProcess,
            String lessonLearned,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            LocalDateTime dueDate,
            TaskStatus status,
            TaskDifficulty difficulty,
            TaskPriority priority
    ) {
        this.project = project;
        this.title = title;
        this.goal = goal;
        this.workProcess = workProcess;
        this.lessonLearned = lessonLearned;
        this.startedAt = startedAt;
        this.finishedAt = finishedAt;
        this.dueDate = dueDate;
        this.status = status == null ? TaskStatus.TODO : status;
        this.difficulty = difficulty == null ? TaskDifficulty.NORMAL : difficulty;
        this.priority = priority == null ? TaskPriority.MEDIUM : priority;
    }

    public void update(
            String title,
            String goal,
            String workProcess,
            String lessonLearned,
            LocalDateTime startedAt,
            LocalDateTime finishedAt,
            LocalDateTime dueDate,
            TaskStatus status,
            TaskDifficulty difficulty,
            TaskPriority priority
    ) {
        if (title != null) {
            this.title = title;
        }
        if (goal != null) {
            this.goal = goal;
        }
        if (workProcess != null) {
            this.workProcess = workProcess;
        }
        if (lessonLearned != null) {
            this.lessonLearned = lessonLearned;
        }
        if (startedAt != null) {
            this.startedAt = startedAt;
        }
        if (finishedAt != null) {
            this.finishedAt = finishedAt;
        }
        if (dueDate != null) {
            this.dueDate = dueDate;
        }
        if (status != null) {
            this.status = status;
        }
        if (difficulty != null) {
            this.difficulty = difficulty;
        }
        if (priority != null) {
            this.priority = priority;
        }
    }
}
