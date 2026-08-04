package com.track.track.kafka.consumer;

import com.track.track.kafka.event.TaskDueSoonEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.track.track.config.KafkaTopicConfig.TASK_NOTIFICATION_EVENTS;

@Component
@Slf4j
public class TaskDueSoonEventConsumer {

    /**
     * Kafka에서 마감 임박 Task 이벤트를 소비한다.
     *
     * @param event 수신한 마감 임박 Task 이벤트
     */
    @KafkaListener(
            topics = TASK_NOTIFICATION_EVENTS,
            groupId = "task-email-notification-group"
    )
    public void consume(TaskDueSoonEvent event) {
        log.info("TaskDueSoonEvent consumed: {}", event);
    }
}