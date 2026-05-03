package org.example.notificationservice.producer;

import org.example.notificationservice.event.DeadlineEvent;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.logging.Logger;

@Service
public class NotificationProducer {

    private static final Logger LOGGER = Logger.getLogger(NotificationProducer.class.getName());
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "task-notifications";

    public NotificationProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendDeadlineNotification(DeadlineEvent event) {
        try {
            kafkaTemplate.send(TOPIC, event.getUserEmail(), event);
            LOGGER.info("Sent event to Kafka: " + event.getEventType() + " for user " + event.getUserEmail());
        } catch (Exception e) {
            LOGGER.severe("Error sending to Kafka: " + e.getMessage());
        }
    }
}