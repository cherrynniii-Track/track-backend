package com.track.track.service.notification;

import com.track.track.exception.BusinessException;
import com.track.track.exception.ErrorCode;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    /**
     * 마감 기한 임박 메일 전송
     * @param recipientEmail 수신 이메일
     * @param taskTitle 작업 제목
     * @param projectName 프로젝트 이름
     * @param dueDate 마감일
     */
    public void sendDeadlineReminder(
            String recipientEmail,
            String taskTitle,
            String projectName,
            LocalDateTime dueDate
    ) {
        MimeMessage message = mailSender.createMimeMessage();

        try {
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            String formattedDueDate = dueDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

            helper.setTo(recipientEmail);
            helper.setSubject("[Track] 마감 임박 알림");

            String content = """
                    <h2>Task 마감이 임박했습니다.</h2>
                    <p><strong>프로젝트:</strong> %s</p>
                    <p><strong>Task:</strong> %s</p>
                    <p><strong>마감일:</strong> %s</p>
                    """.formatted(projectName, taskTitle, formattedDueDate);

            helper.setText(content, true);

            log.info("메일 발송 요청: recipient={}", recipientEmail);
            mailSender.send(message);
            log.info("메일 발송 완료: recipient={}", recipientEmail);
        } catch (MessagingException | MailException e) {
            throw new BusinessException(ErrorCode.MAIL_SEND_FAILED);
        }
    }
}