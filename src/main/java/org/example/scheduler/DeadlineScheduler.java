package org.example.scheduler;

import org.example.entity.Task;
import org.example.event.DeadlineEvent;
import org.example.repository.TaskRepository;
import org.example.service.NotificationProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;

@Component
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class DeadlineScheduler {

    private final TaskRepository taskRepository;
    private final NotificationProducer notificationProducer;

    @Scheduled(cron = "0 */5 * * * *")
    public void checkDeadlines() {
        log.info("Проверка дедлайнов...");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime next24Hours = now.plusHours(24);

        List<Task> upcomingTasks = taskRepository.findByDeadlineBetween(now, next24Hours);

        for (Task task : upcomingTasks) {
            if (!"DONE".equals(task.getStatus())) {
                DeadlineEvent event = new DeadlineEvent(
                        task.getId(),
                        task.getTitle(),
                        task.getDeadline(),
                        task.getUser().getEmail(),
                        task.getUser().getName(),
                        "REMINDER"
                );
                notificationProducer.sendDeadlineNotification(event);
            }
        }

        List<Task> overdueTasks = taskRepository.findByDeadlineBeforeAndStatusNot(now, "DONE");
        for (Task task : overdueTasks) {
            DeadlineEvent event = new DeadlineEvent(
                    task.getId(),
                    task.getTitle(),
                    task.getDeadline(),
                    task.getUser().getEmail(),
                    task.getUser().getName(),
                    "OVERDUE"
            );
            notificationProducer.sendDeadlineNotification(event);
        }
    }
}