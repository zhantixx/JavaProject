package org.example.userservice.config;

import org.example.userservice.service.OAuthAccountService;
import org.example.userservice.utils.JwtUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Instant;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtUtil jwtUtil;
    private final OAuth2AuthorizedClientService authorizedClientService;
    private final OAuthAccountService oAuthAccountService;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthFilter,
                          JwtUtil jwtUtil,
                          OAuth2AuthorizedClientService authorizedClientService,
                          OAuthAccountService oAuthAccountService) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.jwtUtil = jwtUtil;
        this.authorizedClientService = authorizedClientService;
        this.oAuthAccountService = oAuthAccountService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.disable())
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/register", "/api/auth/login", "/oauth2/**", "/login/oauth2/**","/api/calendar/events").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .successHandler((request, response, authentication) -> {
                            OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
                            String email = oauth2User.getAttribute("email");

                            // Сохраняем OAuth2 токены
                            try {
                                String registrationId = "google";
                                OAuth2AuthorizedClient client = authorizedClientService
                                        .loadAuthorizedClient(registrationId, authentication.getName());
                                if (client != null && client.getAccessToken() != null) {
                                    String access = client.getAccessToken().getTokenValue();
                                    Instant expires = client.getAccessToken().getExpiresAt();
                                    String refresh = client.getRefreshToken() != null ? client.getRefreshToken().getTokenValue() : null;
                                    oAuthAccountService.saveOrUpdateGoogle(email, access, refresh, expires);
                                }
                            } catch (Exception ignored) { }

                            String linkParam = request.getParameter("link");
                            if ("true".equals(linkParam)) {
                                // Привязка Google аккаунта к существующему пользователю
                                response.sendRedirect("http://localhost:3000/settings?calendar_linked=true");
                            } else {
                                // Обычный OAuth2 вход – генерируем JWT
                                String token = jwtUtil.generateToken(email, "USER");
                                response.sendRedirect("http://localhost:3000/oauth2/redirect?token=" + token);
                            }
                        })
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }




    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}