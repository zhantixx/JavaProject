package org.example.userservice.service;

import org.example.userservice.dto.AuthRequest;
import org.example.userservice.dto.AuthResponse;
import org.example.userservice.dto.RegisterRequest;
import org.example.userservice.entity.User;
import org.example.userservice.repository.UserRepository;
import org.example.userservice.utils.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final AuditService auditService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil,
                       AuthenticationManager authenticationManager,
                       UserDetailsService userDetailsService,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.auditService = auditService;
    }

    public AuthResponse register(RegisterRequest request, String clientIp) {
        if (request.getConsentGiven() == null || !request.getConsentGiven()) {
            throw new RuntimeException("You must consent to data processing");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setRole("USER");
        user.setConsentGiven(true);
        userRepository.save(user);

        auditService.log(user.getId(), "REGISTER", "Email: " + user.getEmail(), clientIp);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole());
        return new AuthResponse(token);
    }

    public AuthResponse login(AuthRequest request, String clientIp) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));
        auditService.log(user.getId(), "LOGIN", null, clientIp);

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtUtil.generateToken(userDetails.getUsername(), user.getRole());
        return new AuthResponse(token);
    }
}