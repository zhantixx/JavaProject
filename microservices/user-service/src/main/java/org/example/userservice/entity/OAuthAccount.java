package org.example.userservice.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "oauth_accounts", indexes = {
        @Index(name = "idx_oauth_email_provider", columnList = "email,provider", unique = true)
})
public class OAuthAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false, length = 32)
    private String provider; // "google"

    @Column(length = 2048)
    private String accessToken;

    @Column(length = 2048)
    private String refreshToken;

    private Instant accessTokenExpiresAt;

    public OAuthAccount() {}

    public OAuthAccount(String email, String provider) {
        this.email = email;
        this.provider = provider;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getProvider() { return provider; }
    public String getAccessToken() { return accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public Instant getAccessTokenExpiresAt() { return accessTokenExpiresAt; }

    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setProvider(String provider) { this.provider = provider; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public void setAccessTokenExpiresAt(Instant accessTokenExpiresAt) { this.accessTokenExpiresAt = accessTokenExpiresAt; }
}
