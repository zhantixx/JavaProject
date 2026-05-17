package org.example.userservice.utils;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

class JwtUtilTest {

    private static final String SECRET = "404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970";

    private JwtUtil buildWithSecretAndExpiration(long expirationMs) throws Exception {
        JwtUtil util = new JwtUtil();
        Field secretF = JwtUtil.class.getDeclaredField("secret");
        secretF.setAccessible(true);
        secretF.set(util, SECRET);

        Field expF = JwtUtil.class.getDeclaredField("expiration");
        expF.setAccessible(true);
        expF.set(util, expirationMs);
        return util;
    }

    @Test
    void shouldGenerateAndParseToken_withRole() throws Exception {
        JwtUtil util = buildWithSecretAndExpiration(3_600_000L); // 1 С‡Р°СЃ

        String token = util.generateToken("user@example.com", "ADMIN");

        assertThat(util.extractUsername(token)).isEqualTo("user@example.com");
        assertThat(util.extractRole(token)).isEqualTo("ADMIN");
        assertThat(util.validateToken(token, org.springframework.security.core.userdetails.User
                .withUsername("user@example.com")
                .password("N/A").authorities("ROLE_ADMIN").build())).isTrue();
    }

    @Test
    void shouldTreatTokenAsExpired() throws Exception {
        JwtUtil util = buildWithSecretAndExpiration(-1000L); // 1000 РјСЃ
        String token = util.generateToken("expired@example.com", "USER");

        // РџСЂРѕРІРµСЂСЏРµРј С‚РѕРєРµРЅ РїРѕСЃР»Рµ РЅРµР±РѕР»СЊС€РѕР№ РїР°СѓР·С‹ (РёР·Р±РµРіР°СЏ Р»РѕР¶РЅРѕР№ РёСЃС‚РµРєС†РёРё СЃСЂРѕРєР° РґРµР№СЃС‚РІРёСЏ)

        assertThat(util.validateToken(token, org.springframework.security.core.userdetails.User
                .withUsername("expired@example.com")
                .password("N/A").authorities("ROLE_USER").build())).isFalse();
    }
}
