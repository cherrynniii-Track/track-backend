package com.track.track.service.notification;

import com.track.track.domain.Task;
import com.track.track.domain.TaskNotificationHistory;
import com.track.track.enums.NotificationStatus;
import com.track.track.enums.task.TaskStatus;
import com.track.track.exception.BusinessException;
import com.track.track.kafka.event.TaskDueSoonEvent;
import com.track.track.kafka.producer.TaskDueSoonEventProducer;
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
import java.util.UUID;

/**
 * 마감 기한이 임박한 작업의 알림 이벤트를 발행하고, 이벤트를 통해 이메일 알림을 전송
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeadlineNotificationService {

    private final TaskRepository taskRepository;
    private final TaskNotificationHistoryRepository notificationHistoryRepository;
    private final EmailService emailService;
    private final TaskDueSoonEventProducer taskDueSoonEventProducer;
    private final TransactionTemplate transactionTemplate;

    @Value("${notification.deadline.hours-before}")
    private long hoursBefore;

    /**
     * 현재 시각부터 설정된 알림 기준 시간 이내에 마감되는 작업을 조회하고 각 작업의 마감 알림 이벤트를 발행
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
            publishNotification(task);
        }
    }

    /**
     * 알림 이력을 준비한 후 마감 알림 이벤트를 발행
     * 이미 처리 중이거나 전송이 완료된 알림은 다시 발행 X
     * @param task 알림 대상 작업
     */
    private void publishNotification(Task task) {
        LocalDateTime dueDate = task.getDueDate();
        String recipientEmail = task.getProject().getMember().getEmail();

        TaskNotificationHistory history;
        try {
            history = transactionTemplate.execute(status -> prepareHistory(task, dueDate, recipientEmail));
        } catch (DataIntegrityViolationException e) {
            log.info("이미 처리 중인 마감 알림입니다. taskId={}, dueDate={}", task.getId(), dueDate);
            return;
        }

        TaskDueSoonEvent event = new TaskDueSoonEvent(
                UUID.randomUUID(),
                task.getId(),
                task.getTitle(),
                task.getProject().getName(),
                dueDate,
                recipientEmail,
                LocalDateTime.now()
        );
        taskDueSoonEventProducer.publish(event);
    }

    /**
     * 작업의 마감 알림 이력을 생성하거나 재전송 가능한 상태로 변경
     * 기존 이력이 실패 상태인 경우 PENDING으로 변경하며, 그 외 상태라면 중복 처리를 방지하기 위해 {@code null} 반환
     * @param task 알림 대상 작업
     * @param dueDate 작업 마감 일시
     * @param recipientEmail 알림 수신 이메일
     * @return 발행할 알림 이력, 발행 대상이 아니면 {@code null}
     */
    private TaskNotificationHistory prepareHistory(Task task, LocalDateTime dueDate, String recipientEmail) {
        TaskNotificationHistory existingHistory = notificationHistoryRepository
                .findByTaskIdAndDueDate(task.getId(), dueDate)
                .orElse(null);

        if (existingHistory != null) {
            if (existingHistory.getStatus() != NotificationStatus.FAILED) {
                return null;
            }
            existingHistory.markAsPending();
            return existingHistory;
        }

        TaskNotificationHistory pendingHistory = TaskNotificationHistory.builder()
                .task(task)
                .dueDate(dueDate)
                .recipientEmail(recipientEmail)
                .status(NotificationStatus.PENDING)
                .build();
        return notificationHistoryRepository.saveAndFlush(pendingHistory);
    }

    /**
     * 마감 알림 이벤트를 처리하여 이메일 전송하고 알림 이력 상태 갱신
     * @param event 처리할 마감 알림 이벤트
     */
    public void sendNotification(TaskDueSoonEvent event) {
        TaskNotificationHistory history = notificationHistoryRepository
                .findByTaskIdAndDueDate(event.taskId(), event.dueDate())
                .orElse(null);

        if (history == null || history.getStatus() != NotificationStatus.PENDING) {
            log.info("처리할 마감 알림 이력이 없습니다. taskId={}, eventId={}", event.taskId(), event.eventId());
            return;
        }

        try {
            emailService.sendDeadlineReminder(
                    event.recipientEmail(),
                    event.taskTitle(),
                    event.projectName(),
                    event.dueDate()
            );
            updateHistoryStatus(history, true);
        } catch (BusinessException e) {
            updateHistoryStatus(history, false);
            log.error(
                    "마감 알림 이메일 전송 실패. taskId={}, recipient={}",
                    event.taskId(),
                    event.recipientEmail(),
                    e
            );
        }
    }

    /**
     * 이메일 전송 결과에 따라 알림 이력 상태 변경
     * @param history 상탤글 변경할 알림 이력
     * @param sent 전송 성공 여부
     */
    private void updateHistoryStatus(TaskNotificationHistory history, boolean sent) {
        transactionTemplate.executeWithoutResult(status -> {
            if (sent) {
                history.markAsSent();
            } else {
                history.markAsFailed();
            }
            notificationHistoryRepository.save(history);
        });
    }
}
