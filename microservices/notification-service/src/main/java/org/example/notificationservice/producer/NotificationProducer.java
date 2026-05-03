package org.example.notificationservice.producer;

import org.example.notificationservice.event.DeadlineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "task-notifications";

    public void sendDeadlineNotification(DeadlineEvent event) {
        try {
            kafkaTemplate.send(TOPIC, event.getUserEmail(), event);
            log.info("Sent event to Kafka: {} for user {}", event.getEventType(), event.getUserEmail());
        } catch (Exception e) {
            log.error("Error sending to Kafka: {}", e.getMessage());
        }
    }
}