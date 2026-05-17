package org.example.notificationservice.producer;

import org.example.notificationservice.event.DeadlineEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class NotificationProducer {

    private static final Logger log = LoggerFactory.getLogger(NotificationProducer.class);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final String TOPIC = "task-notifications";

    public NotificationProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void sendDeadlineNotification(DeadlineEvent event) {
        kafkaTemplate.send(TOPIC, event.getUserEmail(), event)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish deadline event {} for user {}",
                                event.getEventType(), event.getUserEmail(), ex);
                    } else {
                        log.info("Published deadline event {} for user {}", event.getEventType(), event.getUserEmail());
                    }
                });
    }
}
