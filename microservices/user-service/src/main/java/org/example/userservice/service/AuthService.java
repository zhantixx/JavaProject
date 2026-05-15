package org.example.userservice.service;

import org.example.userservice.dto.AuthRequest;
import org.example.userservice.dto.AuthResponse;
import org.example.userservice.dto.RegisterRequest;
import org.example.userservice.entity.RefreshToken;
import org.example.userservice.entity.User;
import org.example.userservice.repository.RefreshTokenRepository;
import org.example.userservice.repository.UserRepository;
import org.example.userservice.utils.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.time.Instant;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final AuditService auditService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService,
                       AuditService auditService,
                       RefreshTokenRepository refreshTokenRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.auditService = auditService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public AuthResponse register(RegisterRequest request, String clientIp) {
        if (request.getConsentGiven() != null && !request.getConsentGiven()) {
            throw new RuntimeException("You must consent to data processing");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole("USER");
        user.setConsentGiven(Boolean.TRUE.equals(request.getConsentGiven()));
        userRepository.save(user);

        auditService.log(user.getId(), "REGISTER", "Email: " + user.getEmail(), clientIp);

        String token = jwtUtil.generateToken(user.getEmail());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        saveRefreshToken(user.getId(), refreshToken);

        return new AuthResponse(token, refreshToken);
    }

    public AuthResponse login(AuthRequest request, String clientIp) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        auditService.log(user.getId(), "LOGIN", null, clientIp);

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = "USER".equals(user.getRole())
                ? jwtUtil.generateToken(userDetails.getUsername())
                : jwtUtil.generateToken(userDetails.getUsername(), user.getRole());
        String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername());

        refreshTokenRepository.deleteByUserId(user.getId());
        saveRefreshToken(user.getId(), refreshToken);

        return new AuthResponse(token, refreshToken);
    }

    private void saveRefreshToken(Long userId, String refreshToken) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(refreshToken);
        rt.setUserId(userId);
        rt.setExpiresAt(Instant.now().plusMillis(jwtUtil.getRefreshExpiration()));
        refreshTokenRepository.save(rt);
    }
}