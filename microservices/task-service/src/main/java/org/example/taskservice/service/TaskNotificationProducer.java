package org.example.taskservice.service;

import org.example.taskservice.dto.NotificationRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class TaskNotificationProducer {

    private static final String TOPIC = "notification-events";

    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;

    public TaskNotificationProducer(KafkaTemplate<String, NotificationRequest> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void send(NotificationRequest request) {
        kafkaTemplate.send(TOPIC, String.valueOf(request.getUserId()), request);
    }
}
