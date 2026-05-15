package org.example.userservice.controller;

import org.example.userservice.dto.AuthResponse;
import org.example.userservice.entity.RefreshToken;
import org.example.userservice.entity.User;
import org.example.userservice.repository.RefreshTokenRepository;
import org.example.userservice.repository.UserRepository;
import org.example.userservice.utils.JwtUtil;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.time.Instant;

@RestController
@RequestMapping("/api/auth")
public class RefreshTokenController {

    private final JwtUtil jwtUtil;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public RefreshTokenController(JwtUtil jwtUtil,
                                  RefreshTokenRepository refreshTokenRepository,
                                  UserRepository userRepository) {
        this.jwtUtil = jwtUtil;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("Invalid refresh token");
        }
        String refreshToken = authHeader.substring(7);
        RefreshToken stored = refreshTokenRepository.findByTokenAndRevokedFalse(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        if (stored.getExpiresAt().isBefore(Instant.now())) {
            throw new RuntimeException("Refresh token expired");
        }
        if (!jwtUtil.validateRefreshToken(refreshToken)) {
            throw new RuntimeException("Invalid refresh token");
        }
        String username = jwtUtil.extractUsername(refreshToken);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Генерируем новый access token (refresh token оставляем тот же)
        String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return ResponseEntity.ok(new AuthResponse(newAccessToken, refreshToken));
    }
}