package org.example.notificationservice.service;

import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final boolean enabled;
    private final String from;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.mail.enabled:false}") boolean enabled,
                        @Value("${app.mail.from:}") String from) {
        this.mailSender = mailSender;
        this.enabled = enabled;
        this.from = from;
    }

    public void sendPlainText(@NonNull String to, @NonNull String subject, @NonNull String body) {
        send(to, subject, body, false);
    }

    public void sendHtml(@NonNull String to, @NonNull String subject, @NonNull String body) {
        send(to, subject, body, true);
    }

    public void sendReminder(String to, String userName, String taskTitle, String deadline) {
        String subject = "Reminder: " + taskTitle;
        String body = String.format(
                "Hello %s,%nThis is a reminder: Task '%s' is due at %s.%n",
                safe(userName), safe(taskTitle), safe(deadline)
        );
        sendPlainText(to, subject, body);
    }

    public void sendOverdue(String to, String userName, String taskTitle, String deadline) {
        String subject = "Overdue: " + taskTitle;
        String body = String.format(
                "Hello %s,%nTask '%s' became overdue at %s.%n",
                safe(userName), safe(taskTitle), safe(deadline)
        );
        sendPlainText(to, subject, body);
    }

    public void sendCreated(String to, String userName, String taskTitle) {
        String subject = "Task created: " + taskTitle;
        String body = String.format(
                "Hello %s,%nYour task '%s' has been created.%n",
                safe(userName), safe(taskTitle)
        );
        sendPlainText(to, subject, body);
    }

    private void send(String to, String subject, String body, boolean html) {
        if (!enabled) {
            log.debug("Email is disabled (app.mail.enabled=false). Skipping send to {}", to);
            return;
        }
        if (to == null || to.isBlank()) {
            log.warn("Empty recipient email, skip sending. Subject: {}", subject);
            return;
        }
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                    message,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, html);
            if (from != null && !from.isBlank()) {
                helper.setFrom(from);
            }
            mailSender.send(message);
            log.info("Email sent to {} with subject '{}'", to, subject);
        } catch (Exception e) {
            log.error("Failed to send email to {}. Subject '{}'. Error: {}", to, subject, e.getMessage(), e);
        }
    }

    private String safe(String s) {
        return s == null ? "" : s;
    }
}
