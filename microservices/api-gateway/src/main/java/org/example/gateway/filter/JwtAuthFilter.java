package org.example.gateway.filter;

import io.jsonwebtoken.Claims;
import org.example.gateway.utils.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class JwtAuthFilter implements GlobalFilter, Ordered {

    private final JwtUtil jwtUtil;

    public JwtAuthFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    private static final String[] PUBLIC_PATHS = {
            "/api/auth/register",
            "/api/auth/login",
            "/api/auth/refresh",
            "/oauth2/",
            "/login/"
    };

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        ServerHttpRequest sanitizedRequest = exchange.getRequest().mutate()
                .headers(headers -> {
                    headers.remove("X-User-Id");
                    headers.remove("X-Internal-Secret");
                })
                .build();
        ServerWebExchange sanitizedExchange = exchange.mutate().request(sanitizedRequest).build();

        for (String publicPath : PUBLIC_PATHS) {
            if (path.equals(publicPath) || path.startsWith(publicPath)) {
                return chain.filter(sanitizedExchange);
            }
        }

        String authHeader = sanitizedExchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            sanitizedExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return sanitizedExchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);

        try {
            if (!jwtUtil.validateToken(token)) {
                sanitizedExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return sanitizedExchange.getResponse().setComplete();
            }

            Claims claims = jwtUtil.extractAllClaims(token);
            String userId;

            // Попытка извлечь userId из claims
            Object userIdObj = claims.get("userId");
            if (userIdObj == null || String.valueOf(userIdObj).isBlank()) {
                sanitizedExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                return sanitizedExchange.getResponse().setComplete();
            }
            userId = String.valueOf(userIdObj);

            ServerHttpRequest mutatedRequest = sanitizedExchange.getRequest().mutate()
                    .header("X-User-Id", userId)
                    .build();

            return chain.filter(sanitizedExchange.mutate().request(mutatedRequest).build());

        } catch (Exception e) {
            System.err.println("JWT Filter error: " + e.getMessage());
            e.printStackTrace();
            sanitizedExchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return sanitizedExchange.getResponse().setComplete();
        }
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
