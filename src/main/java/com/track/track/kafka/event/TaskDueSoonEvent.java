package com.track.track.kafka.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskDueSoonEvent(
        UUID eventId,
        Long taskId,
        String taskTitle,
        String projectName,
        LocalDateTime dueDate,
        String recipientEmail,
        LocalDateTime occurredAt
) {
}
