package com.track.track.domain;

import com.track.track.enums.TaskDifficulty;
import com.track.track.enums.TaskStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
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
            TaskDifficulty difficulty
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
        this.difficulty = difficulty == null ? TaskDifficulty.MEDIUM : difficulty;
    }
}
