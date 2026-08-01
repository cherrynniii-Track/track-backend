package com.track.track.repository;

import com.track.track.domain.TaskNotificationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface TaskNotificationHistoryRepository
        extends JpaRepository<TaskNotificationHistory, Long> {

    Optional<TaskNotificationHistory> findByTaskIdAndDueDate(
            Long taskId,
            LocalDateTime dueDate
    );
}