package org.example.userservice.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class EmailHashUtilTest {

    @Test
    void sha256_shouldNormalizeEmailBeforeHashing() {
        String first = EmailHashUtil.sha256(" User@Example.COM ");
        String second = EmailHashUtil.sha256("user@example.com");

        assertThat(first).isEqualTo(second);
        assertThat(EmailHashUtil.normalize(" User@Example.COM ")).isEqualTo("user@example.com");
    }
}
