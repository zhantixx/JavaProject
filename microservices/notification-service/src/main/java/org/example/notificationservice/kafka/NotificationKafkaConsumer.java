package org.example.notificationservice.kafka;

import org.example.notificationservice.dto.NotificationRequest;
import org.example.notificationservice.service.NotificationService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationKafkaConsumer {

    private final NotificationService notificationService;

    public NotificationKafkaConsumer(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(topics = "notification-events", groupId = "notification-service")
    public void listen(NotificationRequest request) {
        System.out.println("Received notification request: " + request);
        notificationService.sendNotification(request);
    }
}
