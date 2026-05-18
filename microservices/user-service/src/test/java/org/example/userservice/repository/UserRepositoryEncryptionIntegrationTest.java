package org.example.userservice.repository;

import org.example.userservice.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers(disabledWithoutDocker = true)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class UserRepositoryEncryptionIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("user_service_test")
            .withUsername("postgres")
            .withPassword("postgres")
            .withInitScript("sql/test-pgcrypto-init.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
        registry.add("spring.datasource.hikari.connection-init-sql",
                () -> "SELECT set_config('app.crypto_key', 'test-crypto-key', false)");
    }

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void shouldStoreSensitiveFieldsEncryptedAndReadThemDecrypted() {
        User user = new User();
        user.setEmail("student@example.com");
        user.setPassword("secret");
        user.setName("Auelkhan");
        user.setPhone("+77011234567");

        User saved = userRepository.saveAndFlush(user);

        String rawEmail = jdbcTemplate.queryForObject(
                "select email from users where id = ?",
                String.class,
                saved.getId()
        );
        String rawPhone = jdbcTemplate.queryForObject(
                "select phone from users where id = ?",
                String.class,
                saved.getId()
        );

        assertThat(rawEmail).contains("BEGIN PGP MESSAGE");
        assertThat(rawEmail).doesNotContain("student@example.com");
        assertThat(rawPhone).contains("BEGIN PGP MESSAGE");
        assertThat(rawPhone).doesNotContain("+77011234567");

        User reloaded = userRepository.findById(saved.getId()).orElseThrow();
        assertThat(reloaded.getEmail()).isEqualTo("student@example.com");
        assertThat(reloaded.getPhone()).isEqualTo("+77011234567");
        assertThat(userRepository.findByEmail("student@example.com")).isPresent();
    }
}
