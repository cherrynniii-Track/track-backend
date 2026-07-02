package com.track.track.repository;

import com.track.track.domain.Task;
import com.track.track.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    // 프로젝트의 모든 작업 조회
    List<Task> findByProjectId(Long projectId);
    
    // 상태별 조회
    List<Task> findByStatus(TaskStatus status);
    
    // 특정 프로젝트의 상태별 조회
    List<Task> findByProjectIdAndStatus(Long projectId, TaskStatus status);
    
    // 특정 프로젝트의 마감 기한 순 할 일 조회
    List<Task> findByProjectIdOrderByDueDateAsc(Long projectId);
    
    // 특정 기간 내의 마감일을 가지는 할 일 조회
    List<Task> findByDueDateBetween(LocalDateTime start, LocalDateTime end);
}
