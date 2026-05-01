package org.example.service;

import org.example.event.DeadlineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationConsumer {

    @KafkaListener(topics = "task-notifications", groupId = "task-notification-group")
    public void consumeDeadlineEvent(DeadlineEvent event) {
        log.info("Получено событие из Kafka: {}", event.getEventType());

        switch (event.getEventType()) {
            case "REMINDER":
                sendReminder(event);
                break;
            case "OVERDUE":
                sendOverdueNotification(event);
                break;
            case "CREATED":
                sendTaskCreatedNotification(event);
                break;
            case "UPDATED":
                sendTaskUpdatedNotification(event);
                break;
        }
    }

    private void sendReminder(DeadlineEvent event) {
        log.info("НАПОМИНАНИЕ: Задача '{}' должна быть выполнена до {}",
                event.getTaskTitle(), event.getDeadline());
    }

    private void sendOverdueNotification(DeadlineEvent event) {
        log.warn("ПРОСРОЧЕНА задача: '{}' (дедлайн был {})",
                event.getTaskTitle(), event.getDeadline());
    }

    private void sendTaskCreatedNotification(DeadlineEvent event) {
        log.info("Новая задача: '{}' создана для {}",
                event.getTaskTitle(), event.getUserName());
    }

    private void sendTaskUpdatedNotification(DeadlineEvent event) {
        log.info("Задача '{}' обновлена", event.getTaskTitle());
    }
}
