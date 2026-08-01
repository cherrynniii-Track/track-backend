package com.track.track.service.notification;

import com.track.track.domain.Task;
import com.track.track.domain.TaskNotificationHistory;
import com.track.track.enums.NotificationStatus;
import com.track.track.enums.task.TaskStatus;
import com.track.track.exception.BusinessException;
import com.track.track.repository.TaskNotificationHistoryRepository;
import com.track.track.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeadlineNotificationService {

    private final TaskRepository taskRepository;
    private final TaskNotificationHistoryRepository notificationHistoryRepository;
    private final EmailService emailService;
    private final TransactionTemplate transactionTemplate;

    @Value("${notification.deadline.hours-before}")
    private long hoursBefore;

    /**
     * 마감 임박 작업을 조회하고 이메일 알림을 발송한다.
     */
    public void sendUpcomingDeadlineNotifications() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime notificationEnd = now.plusHours(hoursBefore);

        List<Task> tasks = taskRepository.findTasksDueSoon(
                now,
                notificationEnd,
                List.of(TaskStatus.COMPLETED, TaskStatus.CANCELED)
        );

        for (Task task : tasks) {
            sendNotification(task);
        }
    }

    private void sendNotification(Task task) {
        LocalDateTime dueDate = task.getDueDate();

        String recipientEmail = task.getProject()
                .getMember()
                .getEmail();

        TaskNotificationHistory history;

        try {
            history = transactionTemplate.execute(status -> {
                TaskNotificationHistory existingHistory =
                        notificationHistoryRepository
                                .findByTaskIdAndDueDate(task.getId(), dueDate)
                                .orElse(null);

                if (existingHistory != null) {
                    // FAILED 상태일 때만 다시 전송
                    if (existingHistory.getStatus() != NotificationStatus.FAILED) {
                        return null;
                    }

                    existingHistory.markAsPending();
                    return existingHistory;
                }

                TaskNotificationHistory pendingHistory =
                        TaskNotificationHistory.builder()
                                .task(task)
                                .dueDate(dueDate)
                                .recipientEmail(recipientEmail)
                                .status(NotificationStatus.PENDING)
                                .build();

                return notificationHistoryRepository.saveAndFlush(pendingHistory);
            });
        } catch (DataIntegrityViolationException e) {
            // 다른 서버나 스케줄러가 같은 이력을 먼저 생성한 경우
            log.info(
                    "이미 처리 중인 마감 알림입니다. taskId={}, dueDate={}",
                    task.getId(),
                    dueDate
            );
            return;
        }

        if (history == null) {
            return;
        }

        try {
            emailService.sendDeadlineReminder(
                    recipientEmail,
                    task.getTitle(),
                    task.getProject().getName(),
                    dueDate
            );

            transactionTemplate.executeWithoutResult(status -> {
                history.markAsSent();
                notificationHistoryRepository.save(history);
            });
        } catch (BusinessException e) {
            transactionTemplate.executeWithoutResult(status -> {
                history.markAsFailed();
                notificationHistoryRepository.save(history);
            });

            log.error(
                    "마감 알림 이메일 전송 실패. taskId={}, recipient={}",
                    task.getId(),
                    recipientEmail,
                    e
            );
        }
    }
}