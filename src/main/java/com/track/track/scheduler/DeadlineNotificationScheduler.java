package com.track.track.scheduler;

import com.track.track.service.notification.DeadlineNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DeadlineNotificationScheduler {

    private final DeadlineNotificationService deadlineNotificationService;

    @Scheduled(cron = "${notification.deadline.cron}")
    public void sendUpcomingDeadlineNotifications() {
        log.info("마감 임박 알림 스케줄러 시작");

        deadlineNotificationService.sendUpcomingDeadlineNotifications();

        log.info("마감 임박 알림 스케줄러 종료");
    }
}