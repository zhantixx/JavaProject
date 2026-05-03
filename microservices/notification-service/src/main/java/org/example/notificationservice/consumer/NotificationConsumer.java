package org.example.notificationservice.consumer;

import org.example.notificationservice.event.DeadlineEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationConsumer {

    private static final Logger log = LoggerFactory.getLogger(NotificationConsumer.class);

    @KafkaListener(topics = "task-notifications", groupId = "notification-group")
    public void consume(DeadlineEvent event) {
        log.info("Received event: {}", event.getEventType());

        switch (event.getEventType()) {
            case "REMINDER":
                log.info("Reminder: Task '{}' deadline is {}", event.getTaskTitle(), event.getDeadline());
                break;
            case "OVERDUE":
                log.info("Overdue: Task '{}' was due on {}", event.getTaskTitle(), event.getDeadline());
                break;
            case "CREATED":
                log.info("New task created: '{}' by {}", event.getTaskTitle(), event.getUserName());
                break;
            case "UPDATED":
                log.info("Task updated: '{}'", event.getTaskTitle());
                break;
            default:
                log.info("Unknown event type: {}", event.getEventType());
        }
    }
}