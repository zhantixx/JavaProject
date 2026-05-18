package org.example.notificationservice.controller;

import org.example.notificationservice.dto.NotificationRequest;
import org.example.notificationservice.dto.NotificationResponse;
import org.example.notificationservice.dto.NotificationItemResponse;
import org.example.notificationservice.dto.NotificationSummaryResponse;
import org.example.notificationservice.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> sendNotification(@RequestBody NotificationRequest request) {
        NotificationResponse response = notificationService.sendNotification(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<NotificationItemResponse>> listUserNotifications(@PathVariable Long userId,
                                                                                @RequestHeader("X-User-Id") Long actorUserId) {
        if (!userId.equals(actorUserId)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(notificationService.getUserNotifications(userId));
    }

    @GetMapping("/users/{userId}/summary")
    public ResponseEntity<NotificationSummaryResponse> getSummary(@PathVariable Long userId,
                                                                  @RequestHeader("X-User-Id") Long actorUserId) {
        if (!userId.equals(actorUserId)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(notificationService.getUserSummary(userId));
    }

    @PatchMapping("/users/{userId}/{notificationId}/read")
    public ResponseEntity<NotificationItemResponse> markAsRead(@PathVariable Long userId,
                                                               @PathVariable Long notificationId,
                                                               @RequestHeader("X-User-Id") Long actorUserId) {
        if (!userId.equals(actorUserId)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(notificationService.markAsRead(userId, notificationId));
    }
}
