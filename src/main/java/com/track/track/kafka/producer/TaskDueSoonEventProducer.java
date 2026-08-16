package com.track.track.kafka.producer;

import com.track.track.kafka.event.TaskDueSoonEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.track.track.config.KafkaTopicConfig.TASK_NOTIFICATION_EVENTS;

@Component
@RequiredArgsConstructor
public class TaskDueSoonEventProducer {

    private final KafkaTemplate<String, TaskDueSoonEvent> kafkaTemplate;

    /**
     * 마감 임박 Task 이벤트를 Kafka Topic으로 발행한다
     * @param event 발행할 이벤트
     */
    public void publish(TaskDueSoonEvent event) {
        kafkaTemplate.send(
                TASK_NOTIFICATION_EVENTS,
                event.taskId().toString(),
                event
        );
    }
}