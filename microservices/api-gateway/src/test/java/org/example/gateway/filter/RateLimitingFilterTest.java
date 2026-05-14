package org.example.gateway.filter;

import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.lang.reflect.Field;
import java.net.InetSocketAddress;

import static org.assertj.core.api.Assertions.assertThat;

class RateLimitingFilterTest {

    private RateLimitingFilter buildWithLimit(int rpm) throws Exception {
        RateLimitingFilter filter = new RateLimitingFilter();
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
        RateLimitingFilter filter = buildWithLimit(2); // 2 запроса в минуту
        GatewayFilterChain chain = exchange -> Mono.empty();

        MockServerWebExchange ex1 = exchangeFromIp("127.0.0.1");
        MockServerWebExchange ex2 = exchangeFromIp("127.0.0.1");
        MockServerWebExchange ex3 = exchangeFromIp("127.0.0.1");

        // первые два проходят
        filter.filter(ex1, chain).block();
        filter.filter(ex2, chain).block();

        // третий должен получить 429
        filter.filter(ex3, chain).block();
        assertThat(ex3.getResponse().getStatusCode()).isEqualTo(HttpStatus.TOO_MANY_REQUESTS);
    }
}
