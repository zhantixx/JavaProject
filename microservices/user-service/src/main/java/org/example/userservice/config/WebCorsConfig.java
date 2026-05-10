package org.example.userservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class WebCorsConfig {

    @Value("${app.cors.allowed-origins:http://localhost:3000}")
    private String allowedOriginsProp;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Разрешаем передавать cookie/Authorization
        config.setAllowCredentials(true);
        // Разрешённые источники (через запятую)
        List<String> origins = Arrays.stream(allowedOriginsProp.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .toList();
        config.setAllowedOrigins(origins);
        // Разрешаем все стандартные методы
        config.addAllowedMethod(CorsConfiguration.ALL);
        // Разрешаем любые заголовки
        config.addAllowedHeader(CorsConfiguration.ALL);
        // Кешируем preflight
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        // Применяем ко всем путям (включая /oauth2/** и /login/oauth2/**)
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
