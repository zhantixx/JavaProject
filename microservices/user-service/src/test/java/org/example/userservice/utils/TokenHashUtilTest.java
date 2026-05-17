package org.example.userservice.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TokenHashUtilTest {

    @Test
    void shouldHashTokenDeterministicallyWithoutKeepingRawValue() {
        String token = "refresh-token-value";

        String first = TokenHashUtil.sha256(token);
        String second = TokenHashUtil.sha256(token);

        assertThat(first).isEqualTo(second);
        assertThat(first).isNotEqualTo(token);
        assertThat(first).doesNotContain("refresh-token");
    }
}
