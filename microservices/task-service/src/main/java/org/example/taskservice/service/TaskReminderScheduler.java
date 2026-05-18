package org.example.taskservice.service;

import org.example.taskservice.dto.NotificationRequest;
import org.example.taskservice.dto.UserNotificationProfileResponse;
import org.example.taskservice.entity.Task;
import org.example.taskservice.repository.TaskRepository;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class TaskReminderScheduler {

    private final TaskRepository taskRepository;
    private final TaskNotificationProducer notificationProducer;
    private final RestTemplate restTemplate;

    @Value("${user.service.url:http://user-service:8081}")
    private String userServiceUrl;

    @Value("${task.reminders.window-minutes:60}")
    private long reminderWindowMinutes;

    @Value("${internal.api.secret:local-dev-internal-secret}")
    private String internalApiSecret;

    public TaskReminderScheduler(TaskRepository taskRepository,
                                 TaskNotificationProducer notificationProducer,
                                 RestTemplate restTemplate) {
        this.taskRepository = taskRepository;
        this.notificationProducer = notificationProducer;
        this.restTemplate = restTemplate;
    }

    @Scheduled(cron = "${task.reminders.cron:0 */5 * * * *}")
    public void publishDeadlineNotifications() {
        LocalDateTime now = LocalDateTime.now();
        publishReminders(now);
        publishOverdue(now);
    }

    private void publishReminders(LocalDateTime now) {
        LocalDateTime fallbackWindowEnd = now.plusMinutes(reminderWindowMinutes);
        List<Task> tasks = taskRepository.findByDeadlineBetweenAndReminderSentFalse(now, fallbackWindowEnd);
        for (Task task : tasks) {
            if (isDone(task)) {
                continue;
            }
            UserNotificationProfileResponse profile = loadProfile(task.getUserId());
            long userWindow = profile != null && profile.getReminderWindowMinutes() != null
                    ? profile.getReminderWindowMinutes()
                    : reminderWindowMinutes;

            if (task.getDeadline() == null || task.getDeadline().isAfter(now.plusMinutes(userWindow))) {
                continue;
            }

            if (sendNotification(task, profile, "Task deadline is near", "Reminder: " + task.getTitle() + " is due at " + task.getDeadline())) {
                task.setReminderSent(true);
                taskRepository.save(task);
            }
        }
    }

    private void publishOverdue(LocalDateTime now) {
        List<Task> tasks = taskRepository.findByDeadlineBeforeAndOverdueSentFalse(now);
        for (Task task : tasks) {
            if (isDone(task)) {
                continue;
            }
            UserNotificationProfileResponse profile = loadProfile(task.getUserId());
            if (sendNotification(task, profile, "Task is overdue", "Overdue: " + task.getTitle() + " was due at " + task.getDeadline())) {
                task.setOverdueSent(true);
                taskRepository.save(task);
            }
        }
    }

    private UserNotificationProfileResponse loadProfile(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Secret", internalApiSecret);
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        return restTemplate.exchange(
                userServiceUrl + "/api/internal/users/" + userId + "/notification-profile",
                HttpMethod.GET,
                entity,
                UserNotificationProfileResponse.class
        ).getBody();
    }

    private boolean sendNotification(Task task, UserNotificationProfileResponse profile, String title, String content) {
        if (profile == null) {
            return false;
        }

        boolean sent = false;
        if (Boolean.TRUE.equals(profile.getEmailNotificationsEnabled())
                && profile.getEmail() != null
                && !profile.getEmail().isBlank()) {
            NotificationRequest request = new NotificationRequest();
            request.setUserId(task.getUserId());
            request.setRecipientEmail(profile.getEmail());
            request.setType("EMAIL");
            request.setTitle(title);
            request.setContent(content);
            notificationProducer.send(request);
            sent = true;
        }

        if (Boolean.TRUE.equals(profile.getPushNotificationsEnabled())) {
            NotificationRequest request = new NotificationRequest();
            request.setUserId(task.getUserId());
            request.setRecipientEmail(profile.getEmail());
            request.setType("PUSH");
            request.setTitle(title);
            request.setContent(content);
            notificationProducer.send(request);
            sent = true;
        }

        return sent;
    }

    private boolean isDone(Task task) {
        String status = task.getStatus();
        return status != null && ("DONE".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status));
    }
}
