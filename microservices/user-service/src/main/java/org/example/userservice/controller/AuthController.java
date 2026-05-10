package org.example.userservice.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.example.userservice.dto.AuthRequest;
import org.example.userservice.dto.AuthResponse;
import org.example.userservice.dto.RegisterRequest;
import org.example.userservice.entity.User;
import org.example.userservice.repository.UserRepository;
import org.example.userservice.service.AuthService;
import jakarta.validation.Valid;
import org.example.userservice.service.GdprExportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final GdprExportService gdprExportService;
    private final UserRepository userRepository;

    public AuthController(AuthService authService,
                          GdprExportService gdprExportService,
                          UserRepository userRepository) {
        this.authService = authService;
        this.gdprExportService = gdprExportService;
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request,
                                                 HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(authService.register(request, clientIp));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody AuthRequest request,
                                              HttpServletRequest httpRequest) {
        String clientIp = httpRequest.getRemoteAddr();
        return ResponseEntity.ok(authService.login(request, clientIp));
    }

    @GetMapping("/api/users/me/export")
    public ResponseEntity<byte[]> exportMyData(@AuthenticationPrincipal UserDetails userDetails) throws Exception {
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
        byte[] data = gdprExportService.exportUserData(user.getId());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=user_data.json")
                .contentType(MediaType.APPLICATION_JSON)
                .body(data);
    }
}