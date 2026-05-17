package org.example.gateway.filter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.function.Function;

@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private static final Duration WINDOW = Duration.ofMinutes(1);

    private final Function<String, Mono<Long>> incrementCounter;

    @Value("${rate.limit.requests-per-minute:100}")
    private int requestsPerMinute;

    public RateLimitingFilter(ReactiveStringRedisTemplate redisTemplate) {
        this(key -> redisTemplate.opsForValue()
                .increment(key)
                .flatMap(count -> count == 1
                        ? redisTemplate.expire(key, WINDOW).thenReturn(count)
                        : Mono.just(count)));
    }

    RateLimitingFilter(Function<String, Mono<Long>> incrementCounter) {
        this.incrementCounter = incrementCounter;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String key = "rate-limit:" + getClientIp(exchange) + ":" + Instant.now().getEpochSecond() / WINDOW.toSeconds();

        return incrementCounter.apply(key)
                .flatMap(count -> {
                    if (count <= requestsPerMinute) {
                        return chain.filter(exchange);
                    }
                    exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                    exchange.getResponse().getHeaders().add("X-Rate-Limit-Reset", String.valueOf(WINDOW.toSeconds()));
                    return exchange.getResponse().setComplete();
                })
                .onErrorResume(ex -> chain.filter(exchange));
    }

    private String getClientIp(ServerWebExchange exchange) {
        String forwardedFor = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        if (exchange.getRequest().getRemoteAddress() == null
                || exchange.getRequest().getRemoteAddress().getAddress() == null) {
            return "unknown";
        }
        return exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
    }

    @Override
    public int getOrder() {
        return -2;
    }
}
