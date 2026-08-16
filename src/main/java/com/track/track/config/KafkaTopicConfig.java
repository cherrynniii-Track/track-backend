package com.track.track.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String TASK_NOTIFICATION_EVENTS = "task-notification-events";

    /**
     * 이메일 알림 이벤트를 전달할 Kafka Topic 생성
     * @return task-notification-events Topic
     */
    @Bean
    public NewTopic taskNotificationEventsTopic() {
        return TopicBuilder
                .name(TASK_NOTIFICATION_EVENTS)
                .partitions(1)
                .replicas(1)
                .build();
    }
}
