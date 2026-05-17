package org.example.taskservice.service;

import org.example.taskservice.dto.NotificationRequest;
import org.example.taskservice.dto.UserNotificationProfileResponse;
import org.example.taskservice.entity.Task;
import org.example.taskservice.repository.TaskRepository;
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
        LocalDateTime windowEnd = now.plusMinutes(reminderWindowMinutes);
        List<Task> tasks = taskRepository.findByDeadlineBetweenAndReminderSentFalse(now, windowEnd);
        for (Task task : tasks) {
            if (isDone(task)) {
                continue;
            }
            sendNotification(task, "Task deadline is near", "Reminder: " + task.getTitle() + " is due at " + task.getDeadline());
            task.setReminderSent(true);
            taskRepository.save(task);
        }
    }

    private void publishOverdue(LocalDateTime now) {
        List<Task> tasks = taskRepository.findByDeadlineBeforeAndOverdueSentFalse(now);
        for (Task task : tasks) {
            if (isDone(task)) {
                continue;
            }
            sendNotification(task, "Task is overdue", "Overdue: " + task.getTitle() + " was due at " + task.getDeadline());
            task.setOverdueSent(true);
            taskRepository.save(task);
        }
    }

    private void sendNotification(Task task, String title, String content) {
        UserNotificationProfileResponse profile = restTemplate.getForObject(
                userServiceUrl + "/api/internal/users/" + task.getUserId() + "/notification-profile",
                UserNotificationProfileResponse.class
        );
        if (profile == null || profile.getEmail() == null || profile.getEmail().isBlank()) {
            return;
        }

        NotificationRequest request = new NotificationRequest();
        request.setUserId(task.getUserId());
        request.setRecipientEmail(profile.getEmail());
        request.setType("EMAIL");
        request.setTitle(title);
        request.setContent(content);
        notificationProducer.send(request);
    }

    private boolean isDone(Task task) {
        String status = task.getStatus();
        return status != null && ("DONE".equalsIgnoreCase(status) || "COMPLETED".equalsIgnoreCase(status));
    }
}
