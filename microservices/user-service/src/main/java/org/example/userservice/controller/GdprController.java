package org.example.userservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.example.userservice.entity.User;
import org.example.userservice.repository.UserRepository;
import org.example.userservice.service.AuditService;
import org.example.userservice.service.GdprExportService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/users/me")
public class GdprController {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final AuditService auditService;
    private final RestTemplate restTemplate;

    public GdprController(UserRepository userRepository,
                          ObjectMapper objectMapper,
                          AuditService auditService,
                          RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
        this.auditService = auditService;
        this.restTemplate = restTemplate;
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportMyData(@AuthenticationPrincipal UserDetails userDetails) throws Exception {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String taskServiceUrl = "http://localhost:8082/api/tasks?userId=" + user.getId();
        Object tasks = restTemplate.getForObject(taskServiceUrl, Object.class);

        ExportDataDto exportData = new ExportDataDto(user.getId(), user.getEmail(), user.getName(), tasks);
        byte[] json = objectMapper.writeValueAsBytes(exportData);
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=user_data.json")
                .body(json);
    }

    @DeleteMapping
    public ResponseEntity<Void> deleteMyAccount(@AuthenticationPrincipal UserDetails userDetails,
                                                HttpServletRequest request) {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        restTemplate.delete("http://localhost:8082/api/tasks/user/" + user.getId());

        userRepository.delete(user);
        auditService.log(user.getId(), "DELETE_ACCOUNT", null, request.getRemoteAddr());

        return ResponseEntity.noContent().build();
    }

    static class ExportDataDto {
        public Long userId;
        public String email;
        public String name;
        public Object tasks;
        public ExportDataDto(Long userId, String email, String name, Object tasks) {
            this.userId = userId;
            this.email = email;
            this.name = name;
            this.tasks = tasks;
        }
    }
}