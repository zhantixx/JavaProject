package org.example.taskservice.service;

import org.example.taskservice.dto.NotificationRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TaskNotificationProducer {

    private static final Logger log = LoggerFactory.getLogger(TaskNotificationProducer.class);
    private static final String TOPIC = "notification-events";

    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    public TaskNotificationProducer(KafkaTemplate<String, NotificationRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(NotificationRequest request) {
        kafkaTemplate.send(TOPIC, String.valueOf(request.getUserId()), request)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish notification event for user {}", request.getUserId(), ex);
                    } else {
                        log.info("Published notification event for user {} to topic {}", request.getUserId(), TOPIC);
                    }
                });
    }
}
