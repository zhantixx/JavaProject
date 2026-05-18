package org.example.userservice.service;

import org.example.userservice.entity.OAuthAccount;
import org.example.userservice.repository.OAuthAccountRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.Locale;
import java.util.Map;

@Service
public class OAuthAccountService {

    private final OAuthAccountRepository repository;
    private final RestTemplate restTemplate;

    @Value("${spring.security.oauth2.client.registration.google.client-id:}")
    private String googleClientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret:}")
    private String googleClientSecret;

    private static final String GOOGLE = "google";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";

    public OAuthAccountService(OAuthAccountRepository repository, RestTemplate restTemplate) {
        this.repository = repository;
        this.restTemplate = restTemplate;
    }

    public void saveOrUpdateGoogle(String email, String accessToken, String refreshToken, Instant expiresAt) {
        saveOrUpdateProvider(email, GOOGLE, accessToken, refreshToken, expiresAt);
    }

    public void saveOrUpdateProvider(String email, String provider, String accessToken, String refreshToken, Instant expiresAt) {
        String normalizedProvider = provider == null ? "" : provider.trim().toLowerCase(Locale.ROOT);
        OAuthAccount acc = repository.findByEmailAndProvider(email, normalizedProvider).orElseGet(() ->
                new OAuthAccount(email, normalizedProvider)
        );
        acc.setAccessToken(accessToken);
        if (refreshToken != null && !refreshToken.isBlank()) {
            acc.setRefreshToken(refreshToken);
        }
        acc.setAccessTokenExpiresAt(expiresAt);
        repository.save(acc);
    }

    public String getValidGoogleAccessToken(String email) {
        OAuthAccount acc = repository.findByEmailAndProvider(email, GOOGLE)
                .orElseThrow(() -> new RuntimeException("Google аккаунты табылмады (email сәйкестендірілмеді)"));

        Instant now = Instant.now();
        if (acc.getAccessToken() != null && acc.getAccessTokenExpiresAt() != null
                && acc.getAccessTokenExpiresAt().isAfter(now.plusSeconds(30))) {
            return acc.getAccessToken();
        }
        if (acc.getRefreshToken() == null || acc.getRefreshToken().isBlank()) {
            throw new RuntimeException("Google аккаунтына қайта кіру қажет (refresh token жоқ)");
        }
        // refresh
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
            form.add("client_id", googleClientId);
            form.add("client_secret", googleClientSecret);
            form.add("grant_type", "refresh_token");
            form.add("refresh_token", acc.getRefreshToken());

            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);

            ResponseEntity<Map> resp = restTemplate.exchange(TOKEN_URL, HttpMethod.POST, entity, Map.class);
            if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
                throw new RuntimeException("Google token refresh сәтсіз аяқталды");
            }
            Map<String, Object> body = resp.getBody();
            String newAccess = (String) body.get("access_token");
            Number expiresIn = (Number) body.getOrDefault("expires_in", 3600);

            if (newAccess == null || newAccess.isBlank()) {
                throw new RuntimeException("Жаңартылған access token табылмады");
            }
            acc.setAccessToken(newAccess);
            acc.setAccessTokenExpiresAt(Instant.now().plusSeconds(expiresIn.longValue()));
            repository.save(acc);
            return newAccess;
        } catch (Exception e) {
            throw new RuntimeException("Google token refresh қатесі: " + e.getMessage(), e);
        }
    }
}
