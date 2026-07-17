package com.track.track.repository;

import com.track.track.domain.Task;
import com.track.track.enums.task.TaskDifficulty;
import com.track.track.enums.task.TaskPriority;
import com.track.track.enums.task.TaskStatus;
import com.track.track.repository.projection.TaskDifficultyCount;
import com.track.track.repository.projection.TaskStatusCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long> {

    // 특정 프로젝트의 작업을 페이지 단위로 조회
    Page<Task> findByProjectId(Long projectId, Pageable pageable);

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

    // 특정 프로젝트의 전체 작업 수 조회
    long countByProjectId(Long projectId);

    // 특정 프로젝트의 상태별 작업 수 집계
    @Query("""
            SELECT t.status AS status, COUNT(t) AS count
            FROM Task t
            WHERE t.project.id = :projectId
            GROUP BY t.status
            """)
    List<TaskStatusCount> countByStatusGroup(
            @Param("projectId") Long projectId
    );

    // 특정 프로젝트의 난이도별 작업 수 집계
    @Query("""
            SELECT t.difficulty AS difficulty, COUNT(t) AS count
            FROM Task t
            WHERE t.project.id = :projectId
            GROUP BY t.difficulty
            """)
    List<TaskDifficultyCount> countByDifficultyGroup(
            @Param("projectId") Long projectId
    );

    // 마감일이 지났지만 완료되거나 취소되지 않은 작업 수 조회
    @Query("""
            SELECT COUNT(t)
            FROM Task t
            WHERE t.project.id = :projectId
              AND t.dueDate < :now
              AND t.status <> :completedStatus
              AND t.status <> :canceledStatus
            """)
    long countOverdueTasks(
            @Param("projectId") Long projectId,
            @Param("now") LocalDateTime now,
            @Param("completedStatus") TaskStatus completedStatus,
            @Param("canceledStatus") TaskStatus canceledStatus
    );

    @Query(
            value = """
                select distinct t
                from Task t
                left join t.categories c
                where t.project.id = :projectId
                  and (:status is null or t.status = :status)
                  and (:difficulty is null or t.difficulty = :difficulty)
                  and (:priority is null or t.priority = :priority)
                  and (:categoryId is null or c.id = :categoryId)
                  and (:dueDateFrom is null or t.dueDate >= :dueDateFrom)
                  and (:dueDateTo is null or t.dueDate <= :dueDateTo)
                """,
            countQuery = """
                select count(distinct t)
                from Task t
                left join t.categories c
                where t.project.id = :projectId
                  and (:status is null or t.status = :status)
                  and (:difficulty is null or t.difficulty = :difficulty)
                  and (:priority is null or t.priority = :priority)
                  and (:categoryId is null or c.id = :categoryId)
                  and (:dueDateFrom is null or t.dueDate >= :dueDateFrom)
                  and (:dueDateTo is null or t.dueDate <= :dueDateTo)
                """
    )
    Page<Task> findTasks(
            @Param("projectId") Long projectId,
            @Param("status") TaskStatus status,
            @Param("difficulty") TaskDifficulty difficulty,
            @Param("priority") TaskPriority priority,
            @Param("categoryId") Long categoryId,
            @Param("dueDateFrom") LocalDateTime dueDateFrom,
            @Param("dueDateTo") LocalDateTime dueDateTo,
            Pageable pageable
    );
}