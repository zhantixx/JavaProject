package org.example.userservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.userservice.entity.User;
import org.example.userservice.repository.RefreshTokenRepository;
import org.example.userservice.repository.UserRepository;
import org.example.userservice.service.AuditService;
import org.example.userservice.service.GdprExportService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/users/me")
public class GdprController {

    private final UserRepository userRepository;
    private final GdprExportService gdprExportService;
    private final AuditService auditService;
    private final RestTemplate restTemplate;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${task.service.url:http://task-service:8082}")
    private String taskServiceUrl;

    @Value("${internal.api.secret}")
    private String internalApiSecret;

    public GdprController(UserRepository userRepository,
                          GdprExportService gdprExportService,
                          AuditService auditService,
                          RestTemplate restTemplate,
                          RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.gdprExportService = gdprExportService;
        this.auditService = auditService;
        this.restTemplate = restTemplate;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportMyData(@AuthenticationPrincipal UserDetails userDetails) throws Exception {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        byte[] json = gdprExportService.exportUserData(user.getId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user_data.json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(json);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMyAccount(@AuthenticationPrincipal UserDetails userDetails,
                                                HttpServletRequest request) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        LocalDateTime now = LocalDateTime.now();
        user.setActive(false);
        user.setDeletionRequestedAt(now);
        user.setScheduledDeletionAt(now.plusDays(30));
        userRepository.save(user);
        refreshTokenRepository.deleteByUserId(user.getId());

        auditService.log(user.getId(), "REQUEST_DELETE_ACCOUNT", "Scheduled for " + user.getScheduledDeletionAt(), request.getRemoteAddr());
        return ResponseEntity.accepted().build();
    }
}
