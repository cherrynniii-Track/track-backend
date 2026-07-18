package com.track.track.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "category_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "project_id")
    private Project project;

    @Column(nullable = false, length = 20)
    private String name;

    @ManyToMany
    @JoinTable(
            name = "category_task",
            joinColumns = @JoinColumn(name = "category_id"),
            inverseJoinColumns = @JoinColumn(name = "task_id"),
            uniqueConstraints = @UniqueConstraint(
            name = "uk_category_task_category_id_task_id",
            columnNames = {"category_id", "task_id"}
    )
    )
    private List<Task> tasks = new ArrayList<>();

    @Builder
    public Category(Project project, String name) {
        this.project = project;
        this.name = name;
    }

    public void update(String name) {
        if (name != null) {
            this.name = name;
        }
    }

    public void addTask(Task task) {
        tasks.add(task);
        task.getCategories().add(this);
    }

    public void removeTask(Task task) {
        tasks.remove(task);
        task.getCategories().remove(this);
    }
}