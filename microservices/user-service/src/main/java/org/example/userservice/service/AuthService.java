package org.example.userservice.service;

import org.example.userservice.dto.AuthRequest;
import org.example.userservice.dto.AuthResponse;
import org.example.userservice.dto.RegisterRequest;
import org.example.userservice.entity.RefreshToken;
import org.example.userservice.entity.User;
import org.example.userservice.repository.RefreshTokenRepository;
import org.example.userservice.repository.UserRepository;
import org.example.userservice.utils.JwtUtil;
import org.example.userservice.utils.TokenHashUtil;
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
    private final LoginAttemptService loginAttemptService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService,
                       AuditService auditService,
                       RefreshTokenRepository refreshTokenRepository,
                       LoginAttemptService loginAttemptService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.auditService = auditService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.loginAttemptService = loginAttemptService;
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

        String token = jwtUtil.generateToken(user.getEmail(), "USER", user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(user.getEmail());

        saveRefreshToken(user.getId(), refreshToken);

        return new AuthResponse(token, refreshToken);
    }

    public AuthResponse login(AuthRequest request, String clientIp) {
        String email = request.getEmail();

        if (loginAttemptService.isBlocked(email)) {
            throw new RuntimeException("Too many failed attempts. Please try again later.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
            loginAttemptService.loginSucceeded(email);
        } catch (Exception e) {
            loginAttemptService.loginFailed(email);
            throw e;
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        auditService.log(user.getId(), "LOGIN", null, clientIp);

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String token = jwtUtil.generateToken(userDetails.getUsername(), user.getRole(), user.getId());
        String refreshToken = jwtUtil.generateRefreshToken(userDetails.getUsername());

        refreshTokenRepository.deleteByUserId(user.getId());
        saveRefreshToken(user.getId(), refreshToken);

        return new AuthResponse(token, refreshToken);
    }

    private void saveRefreshToken(Long userId, String refreshToken) {
        RefreshToken rt = new RefreshToken();
        rt.setToken(TokenHashUtil.sha256(refreshToken));
        rt.setUserId(userId);
        rt.setExpiresAt(Instant.now().plusMillis(jwtUtil.getRefreshExpiration()));
        refreshTokenRepository.save(rt);
    }
}
