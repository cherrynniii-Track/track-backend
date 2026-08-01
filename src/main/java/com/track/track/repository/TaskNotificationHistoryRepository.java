package com.track.track.repository;

import com.track.track.domain.TaskNotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface TaskNotificationHistoryRepository extends JpaRepository<TaskNotificationHistory, Long> {
    boolean existsByTaskIdAndDueDate(Long taskId, LocalDateTime dueDate);
}
