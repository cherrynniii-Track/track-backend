package com.track.track.repository;

import com.track.track.domain.Task;
import com.track.track.enums.task.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // 특정 프로젝트의 작업 목록 조회
    List<Task> findByProjectId(Long projectId);

    // 특정 프로젝트에 속한 작업 단건 조회
    Optional<Task> findByIdAndProjectId(Long taskId, Long projectId);

    // 특정 프로젝트의 상태별 작업 조회
    List<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status);

    // 특정 프로젝트의 작업을 마감일 오름차순으로 조회
    List<Task> findByProjectIdOrderByDueDateAsc(Long projectId);

    // 특정 프로젝트에서 일정 기간 내 마감되는 작업 조회
    List<Task> findByProjectIdAndDueDateBetween(
            Long projectId,
            LocalDateTime start,
            LocalDateTime end
    );

    // 특정 카테고리의 작업 조회
    List<Task> findDistinctByProjectIdAndCategoriesIdOrderByDueDateAsc(
            Long projectId,
            Long categoryId
    );
}