package org.example.notificationservice.service;

import org.example.notificationservice.dto.NotificationRequest;
import org.example.notificationservice.dto.NotificationResponse;
import org.example.notificationservice.entity.NotificationEntity;
import org.example.notificationservice.entity.NotificationEntity.NotificationStatus;
import org.example.notificationservice.entity.NotificationEntity.NotificationType;
import org.example.notificationservice.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final EmailService emailService;

    @Autowired
    public NotificationService(NotificationRepository notificationRepository, EmailService emailService) {
        this.notificationRepository = notificationRepository;
        this.emailService = emailService;
    }

    public NotificationResponse sendNotification(NotificationRequest request) {
        NotificationEntity notification = new NotificationEntity();
        notification.setUserId(request.getUserId());
        notification.setRecipientEmail(request.getRecipientEmail());
        notification.setType(request.getType().equalsIgnoreCase("EMAIL") ? NotificationType.EMAIL : NotificationType.PUSH);
        notification.setTitle(request.getTitle());
        notification.setContent(request.getContent());
        notification.setStatus(NotificationStatus.PENDING);
        notification.setCreatedAt(LocalDateTime.now());

        notification = notificationRepository.save(notification);

        if (notification.getType() == NotificationType.EMAIL) {
            if (notification.getRecipientEmail() == null || notification.getRecipientEmail().isBlank()) {
                notification.setStatus(NotificationStatus.FAILED);
                notificationRepository.save(notification);
                NotificationResponse response = new NotificationResponse();
                response.setId(notification.getId());
                response.setStatus(notification.getStatus().toString());
                return response;
            }

            boolean sent = emailService.sendEmail(
                    notification.getRecipientEmail(),
                    notification.getTitle(),
                    notification.getContent()
            );
            if (sent) {
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
            } else {
                notification.setStatus(NotificationStatus.FAILED);
            }
        } else {
            // Push notification placeholder
            boolean sent = sendPushNotification(notification.getUserId(), notification.getTitle(), notification.getContent());
            if (sent) {
                notification.setStatus(NotificationStatus.SENT);
                notification.setSentAt(LocalDateTime.now());
            } else {
                notification.setStatus(NotificationStatus.FAILED);
            }
        }

        notificationRepository.save(notification);

        NotificationResponse response = new NotificationResponse();
        response.setId(notification.getId());
        response.setStatus(notification.getStatus().toString());
        return response;
    }

    private boolean sendPushNotification(Long userId, String title, String content) {
        // TODO: Implement push notification via Firebase or other service
        // For now, simulate success
        System.out.println("Push notification sent to user " + userId + ": " + title);
        return true;
    }
}
