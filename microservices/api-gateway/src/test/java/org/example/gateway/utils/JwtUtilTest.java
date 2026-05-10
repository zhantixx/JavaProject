package org.example.gateway.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.security.Key;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private JwtUtil buildWithSecret() throws Exception {
        JwtUtil util = new JwtUtil();
        Field f = JwtUtil.class.getDeclaredField("secret");
        f.setAccessible(true);
        f.set(util, SECRET);
        return util;
    }

    private String buildToken(String subject, long ttlMillis) {
        Key key = Keys.hmacShaKeyFor(SECRET.getBytes());
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + ttlMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    @Test
    void shouldExtractUsernameAndValidate_whenTokenValid() throws Exception {
        JwtUtil util = buildWithSecret();
        String token = buildToken("test@example.com", 60_000);

        assertThat(util.extractUsername(token)).isEqualTo("test@example.com");
        assertThat(util.validateToken(token)).isTrue();
    }

    @Test
    void shouldDetectExpiredToken() throws Exception {
        JwtUtil util = buildWithSecret();
        String token = buildToken("expired@example.com", 1);

        // чуть подождём, чтобы истёк срок
        try { Thread.sleep(5L); } catch (InterruptedException ignored) {}

        assertThat(util.validateToken(token)).isFalse();
    }
}
