package com.track.track.domain;

import com.track.track.enums.NotificationStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Entity
@Table(
        name = "task_notification_history",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_task_notification_task_due_date",
                        columnNames = {"task_id", "due_date"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TaskNotificationHistory extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "task_notification_history_id")
    private Long id;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;

    @Column(nullable = false)
    private LocalDateTime dueDate;

    @Column(nullable = false)
    private String recipientEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationStatus status;

    private LocalDateTime sentAt;

    @Builder
    public TaskNotificationHistory(
            Task task,
            LocalDateTime dueDate,
            String recipientEmail,
            NotificationStatus status,
            LocalDateTime sentAt
    ) {
        this.task = task;
        this.dueDate = dueDate;
        this.recipientEmail = recipientEmail;
        this.status = status == null ? NotificationStatus.PENDING : status;
        this.sentAt = sentAt;
    }

    public void markAsSent() {
        this.status = NotificationStatus.SENT;
        this.sentAt = LocalDateTime.now();
    }

    public void markAsFailed() {
        this.status = NotificationStatus.FAILED;
    }

    public void markAsPending() {
        this.status = NotificationStatus.PENDING;
        this.sentAt = null;
    }
}