package org.example.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.example.gateway.utils.JwtUtil;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class JwtAuthFilterTest {

    @Test
    void shouldRejectTokenWithoutUserIdClaim() {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        JwtAuthFilter filter = new JwtAuthFilter(jwtUtil);
        String token = "token-without-user-id";
        Claims claims = Jwts.claims().setSubject("user@example.com");

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractAllClaims(token)).thenReturn(claims);

        MockServerWebExchange exchange = exchange(token);
        filter.filter(exchange, chain -> Mono.empty()).block();

        assertThat(exchange.getResponse().getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void shouldForwardUserIdHeaderFromJwtClaim() {
        JwtUtil jwtUtil = mock(JwtUtil.class);
        JwtAuthFilter filter = new JwtAuthFilter(jwtUtil);
        String token = "token-with-user-id";
        Claims claims = Jwts.claims().setSubject("user@example.com");
        claims.put("userId", "42");
        AtomicReference<String> forwardedUserId = new AtomicReference<>();

        when(jwtUtil.validateToken(token)).thenReturn(true);
        when(jwtUtil.extractAllClaims(token)).thenReturn(claims);

        GatewayFilterChain chain = exchange -> {
            forwardedUserId.set(exchange.getRequest().getHeaders().getFirst("X-User-Id"));
            return Mono.empty();
        };

        filter.filter(exchange(token), chain).block();

        assertThat(forwardedUserId).hasValue("42");
    }

    private MockServerWebExchange exchange(String token) {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/tasks")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();
        return MockServerWebExchange.from(request);
    }
}
