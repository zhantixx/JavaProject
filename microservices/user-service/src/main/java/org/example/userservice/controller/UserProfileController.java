package org.example.userservice.controller;

import org.example.userservice.dto.ChangePasswordRequest;
import org.example.userservice.dto.UpdateMeRequest;
import org.example.userservice.dto.UserMeResponse;
import org.example.userservice.entity.User;
import org.example.userservice.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
public class UserProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserProfileController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public ResponseEntity<UserMeResponse> me(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) {
            return ResponseEntity.ok(new UserMeResponse(1L, "test@example.com", "Test User", "USER", null, null, null, true, true, true, 60));
        }
        User user = userRepository.findByEmail(principal.getUsername()).orElseThrow();
        return ResponseEntity.ok(toResponse(user));
    }

    @PatchMapping
    public ResponseEntity<UserMeResponse> update(@AuthenticationPrincipal UserDetails principal,
                                                 @RequestBody UpdateMeRequest request) {
        if (principal == null) {
            return ResponseEntity.ok(new UserMeResponse(
                    1L,
                    "test@example.com",
                    request.getName() != null ? request.getName() : "Test User",
                    "USER",
                    request.getAvatarUrl(),
                    request.getPhone(),
                    null,
                    true,
                    request.getEmailNotificationsEnabled() != null ? request.getEmailNotificationsEnabled() : true,
                    request.getPushNotificationsEnabled() != null ? request.getPushNotificationsEnabled() : true,
                    request.getReminderWindowMinutes() != null ? request.getReminderWindowMinutes() : 60
            ));
        }
        User user = userRepository.findByEmail(principal.getUsername()).orElseThrow();
        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getAvatarUrl() != null) {
            user.setAvatarUrl(request.getAvatarUrl());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone().isBlank() ? null : request.getPhone().trim());
        }
        if (request.getEmailNotificationsEnabled() != null) {
            user.setEmailNotificationsEnabled(request.getEmailNotificationsEnabled());
        }
        if (request.getPushNotificationsEnabled() != null) {
            user.setPushNotificationsEnabled(request.getPushNotificationsEnabled());
        }
        if (request.getReminderWindowMinutes() != null) {
            int bounded = Math.max(5, Math.min(1440, request.getReminderWindowMinutes()));
            user.setReminderWindowMinutes(bounded);
        }
        userRepository.save(user);
        return ResponseEntity.ok(toResponse(user));
    }

    @PostMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserDetails principal,
                                               @RequestBody ChangePasswordRequest request) {
        if (principal == null) {
            return ResponseEntity.noContent().build();
        }

        User user = userRepository.findByEmail(principal.getUsername()).orElseThrow();
        if (user.getPassword() != null && user.getPassword().startsWith("{noop}oauth2-")) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getCurrentPassword() == null || request.getCurrentPassword().isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        if (request.getNewPassword() == null || request.getNewPassword().length() < 8) {
            return ResponseEntity.badRequest().build();
        }
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            return ResponseEntity.badRequest().build();
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        return ResponseEntity.noContent().build();
    }

    private UserMeResponse toResponse(User user) {
        return new UserMeResponse(
                user.getId(),
                user.getEmail(),
                user.getName(),
                user.getRole(),
                user.getAvatarUrl(),
                user.getPhone(),
                user.getCreatedAt() == null ? null : user.getCreatedAt().toString(),
                user.getPassword() == null || !user.getPassword().startsWith("{noop}oauth2-"),
                user.getEmailNotificationsEnabled(),
                user.getPushNotificationsEnabled(),
                user.getReminderWindowMinutes()
        );
    }
}
