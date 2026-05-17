package org.example.userservice.config;

import org.example.userservice.entity.User;
import org.example.userservice.repository.UserRepository;
import org.example.userservice.service.OAuthAccountService;
import org.example.userservice.utils.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.UUID;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtUtil jwtUtil;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final OAuthAccountService oAuthAccountService;
    private final UserRepository userRepository;

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/redirect}")
    private String oauthRedirectUri;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          JwtUtil jwtUtil,
                          OAuth2AuthorizedClientService authorizedClientService,
                          OAuthAccountService oAuthAccountService,
                          UserRepository userRepository) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.jwtUtil = jwtUtil;
        this.authorizedClientService = authorizedClientService;
        this.oAuthAccountService = oAuthAccountService;
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.disable())
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/api/auth/providers", "/api/internal/**", "/oauth2/**", "/login/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth.successHandler(this::handleOAuthSuccess))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private void handleOAuthSuccess(jakarta.servlet.http.HttpServletRequest request,
                                    jakarta.servlet.http.HttpServletResponse response,
                                    Authentication authentication) throws java.io.IOException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String registrationId = request.getRequestURI().substring(request.getRequestURI().lastIndexOf('/') + 1);
        String email = resolveEmail(principal, registrationId);
        String name = resolveName(principal, email);

        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User created = new User();
            created.setEmail(email);
            created.setName(name);
            created.setRole("USER");
            created.setPassword("{noop}oauth2-" + UUID.randomUUID());
            created.setConsentGiven(true);
            return userRepository.save(created);
        });

        OAuth2AuthorizedClient client = authorizedClientService.loadAuthorizedClient(
                registrationId,
                authentication.getName()
        );
        if ("google".equalsIgnoreCase(registrationId) && client != null && client.getAccessToken() != null) {
            Instant expiresAt = client.getAccessToken().getExpiresAt();
            String refreshToken = client.getRefreshToken() == null ? null : client.getRefreshToken().getTokenValue();
            oAuthAccountService.saveOrUpdateGoogle(
                    email,
                    client.getAccessToken().getTokenValue(),
                    refreshToken,
                    expiresAt
            );
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole(), user.getId());
        String redirect = UriComponentsBuilder.fromUriString(oauthRedirectUri)
                .queryParam("token", token)
                .build()
                .toUriString();
        response.sendRedirect(redirect);
    }

    private String resolveEmail(OAuth2User principal, String registrationId) {
        Object email = principal.getAttribute("email");
        if (email != null && !email.toString().isBlank()) {
            return email.toString();
        }
        Object login = principal.getAttribute("login");
        if (login != null && !login.toString().isBlank()) {
            return login + "@" + registrationId + ".oauth.local";
        }
        return principal.getName() + "@" + registrationId + ".oauth.local";
    }

    private String resolveName(OAuth2User principal, String fallbackEmail) {
        Object name = principal.getAttribute("name");
        if (name != null && !name.toString().isBlank()) {
            return name.toString();
        }
        return fallbackEmail;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}
