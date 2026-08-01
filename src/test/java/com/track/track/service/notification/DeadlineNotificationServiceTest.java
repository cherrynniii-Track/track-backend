package com.track.track.service.notification;

import com.track.track.domain.Member;
import com.track.track.domain.Project;
import com.track.track.domain.Task;
import com.track.track.domain.TaskNotificationHistory;
import com.track.track.enums.task.TaskStatus;
import com.track.track.repository.TaskNotificationHistoryRepository;
import com.track.track.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeadlineNotificationServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TaskNotificationHistoryRepository notificationHistoryRepository;

    @Mock
    private EmailService emailService;

    @Mock
    private TransactionTemplate transactionTemplate;

    @Mock
    private TransactionStatus transactionStatus;

    @InjectMocks
    private DeadlineNotificationService deadlineNotificationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(
                deadlineNotificationService,
                "hoursBefore",
                24L
        );

        lenient().when(transactionTemplate.execute(any()))
                .thenAnswer(invocation -> {
                    TransactionCallback<?> callback =
                            invocation.getArgument(0);

                    return callback.doInTransaction(transactionStatus);
                });

        lenient().doAnswer(invocation -> {
            Consumer<TransactionStatus> callback =
                    invocation.getArgument(0);

            callback.accept(transactionStatus);
            return null;
        }).when(transactionTemplate).executeWithoutResult(any());
    }

    @Test
    @DisplayName("마감 임박 작업 이메일 발송 성공")
    void sendUpcomingDeadlineNotifications_success() {
        LocalDateTime dueDate =
                LocalDateTime.now().plusHours(12);

        Member member = mock(Member.class);
        Project project = mock(Project.class);
        Task task = mock(Task.class);
        TaskNotificationHistory history = mock(TaskNotificationHistory.class);

        when(task.getId()).thenReturn(1L);
        when(task.getTitle()).thenReturn("알림 테스트");
        when(task.getDueDate()).thenReturn(dueDate);
        when(task.getProject()).thenReturn(project);

        when(project.getName()).thenReturn("Track");
        when(project.getMember()).thenReturn(member);
        when(member.getEmail()).thenReturn("notification@test.com");

        when(taskRepository.findTasksDueSoon(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                eq(List.of(
                        TaskStatus.COMPLETED,
                        TaskStatus.CANCELED
                ))
        )).thenReturn(List.of(task));

        when(notificationHistoryRepository
                .existsByTaskIdAndDueDate(1L, dueDate))
                .thenReturn(false);

        when(notificationHistoryRepository.saveAndFlush(any()))
                .thenReturn(history);

        deadlineNotificationService
                .sendUpcomingDeadlineNotifications();

        verify(emailService).sendDeadlineReminder(
                "notification@test.com",
                "알림 테스트",
                "Track",
                dueDate
        );

        verify(history).markAsSent();
        verify(notificationHistoryRepository).save(history);
    }

    @Test
    @DisplayName("이미 처리된 마감 알림은 중복 발송하지 않는다")
    void sendUpcomingDeadlineNotifications_duplicate_skip() {
        LocalDateTime dueDate =
                LocalDateTime.now().plusHours(12);

        Task task = mock(Task.class);

        when(task.getId()).thenReturn(1L);
        when(task.getDueDate()).thenReturn(dueDate);

        when(taskRepository.findTasksDueSoon(
                any(LocalDateTime.class),
                any(LocalDateTime.class),
                anyList()
        )).thenReturn(List.of(task));

        when(notificationHistoryRepository
                .existsByTaskIdAndDueDate(1L, dueDate))
                .thenReturn(true);

        deadlineNotificationService
                .sendUpcomingDeadlineNotifications();

        verify(emailService, never())
                .sendDeadlineReminder(
                        anyString(),
                        anyString(),
                        anyString(),
                        any(LocalDateTime.class)
                );

        verify(notificationHistoryRepository, never())
                .saveAndFlush(any());
    }
}