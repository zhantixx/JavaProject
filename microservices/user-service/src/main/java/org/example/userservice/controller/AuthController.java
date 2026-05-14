package org.example.userservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.userservice.dto.AuthRequest;
import org.example.userservice.dto.AuthResponse;
import org.example.userservice.dto.RegisterRequest;
import org.example.userservice.entity.User;
import org.example.userservice.repository.UserRepository;
import org.example.userservice.service.AuthService;
import org.example.userservice.service.GdprExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final GdprExportService gdprExportService;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;

    public AuthController(AuthService authService,
                          GdprExportService gdprExportService,
                          UserRepository userRepository,
                          RestTemplate restTemplate) {
        this.authService = authService;
        this.gdprExportService = gdprExportService;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody RegisterRequest request,
                                                 HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.register(request, httpRequest.getRemoteAddr()));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody AuthRequest request,
                                              HttpServletRequest httpRequest) {
        return ResponseEntity.ok(authService.login(request, httpRequest.getRemoteAddr()));
    }

    @GetMapping("/api/users/me/export")
    public ResponseEntity<byte[]> exportMyData(@AuthenticationPrincipal UserDetails userDetails) throws Exception {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        byte[] data = gdprExportService.exportUserData(user.getId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user_data.json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
    }

    @DeleteMapping("/api/users/me")
    public ResponseEntity<Void> deleteMyAccount(@AuthenticationPrincipal UserDetails userDetails,
                                                HttpServletRequest request) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        restTemplate.delete("http://localhost:8082/api/tasks/user/" + user.getId());
        userRepository.delete(user);
        return ResponseEntity.noContent().build();
    }
}