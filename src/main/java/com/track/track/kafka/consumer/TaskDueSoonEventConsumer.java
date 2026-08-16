package com.track.track.kafka.consumer;

import com.track.track.kafka.event.TaskDueSoonEvent;
import com.track.track.service.notification.DeadlineNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.track.track.config.KafkaTopicConfig.TASK_NOTIFICATION_EVENTS;

@Component
@Slf4j
@RequiredArgsConstructor
public class TaskDueSoonEventConsumer {

    private final DeadlineNotificationService deadlineNotificationService;

    /**
     * Kafka에서 마감 임박 Task 이벤트를 소비
     * @param event 수신한 마감 임박 Task 이벤트
     */
    @KafkaListener(
            topics = TASK_NOTIFICATION_EVENTS,
            groupId = "task-email-notification-group"
    )
    public void consume(TaskDueSoonEvent event) {
        log.info("TaskDueSoonEvent consumed: {}", event);
        deadlineNotificationService.sendNotification(event);
    }
}
