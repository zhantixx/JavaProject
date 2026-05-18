package org.example.userservice.controller;

import org.example.userservice.dto.UserNotificationProfileResponse;
import org.example.userservice.entity.User;
import org.example.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;

@RestController
@RequestMapping("/api/internal/users")
public class InternalUserController {

    private final UserRepository userRepository;

    @Value("${internal.api.secret}")
    private String internalApiSecret;

    public InternalUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/{id}/notification-profile")
    public ResponseEntity<UserNotificationProfileResponse> notificationProfile(@PathVariable Long id,
                                                                               @RequestHeader(value = "X-Internal-Secret", required = false) String providedSecret) {
        requireInternalSecret(providedSecret);
        User user = userRepository.findById(id).orElseThrow();
        return ResponseEntity.ok(new UserNotificationProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getEmailNotificationsEnabled(),
                user.getPushNotificationsEnabled(),
                user.getReminderWindowMinutes()
        ));
    }

    private void requireInternalSecret(String providedSecret) {
        if (internalApiSecret == null || internalApiSecret.isBlank()
                || providedSecret == null
                || !internalApiSecret.equals(providedSecret)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Internal endpoint");
        }
    }
}
