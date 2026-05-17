package org.example.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.util.concurrent.atomic.AtomicLong;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitingFilterTest {

    private RateLimitingFilter buildWithLimit(int rpm, AtomicLong counter) throws Exception {
        RateLimitingFilter filter = new RateLimitingFilter(key -> Mono.just(counter.incrementAndGet()));
        Field f = RateLimitingFilter.class.getDeclaredField("requestsPerMinute");
        f.setAccessible(true);
        f.set(filter, rpm);
        return filter;
    }

    private MockServerWebExchange exchangeFromIp(String ip) {
        MockServerHttpRequest request = MockServerHttpRequest.get("/any")
                .remoteAddress(new InetSocketAddress(ip, 12345))
                .build();
        return MockServerWebExchange.from(request);
    }

    @Test
    void shouldAllowRequestsUntilLimitAndThenReturn429() throws Exception {
        RateLimitingFilter filter = buildWithLimit(2, new AtomicLong());
        GatewayFilterChain chain = exchange -> Mono.empty();

        filter.filter(exchangeFromIp("127.0.0.1"), chain).block();
        filter.filter(exchangeFromIp("127.0.0.1"), chain).block();

        MockServerWebExchange blocked = exchangeFromIp("127.0.0.1");
        filter.filter(blocked, chain).block();
        assertThat(blocked.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
}
