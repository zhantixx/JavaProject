package org.example.userservice.service;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptService {

    private static final String KEY_PREFIX = "login:fail:";
    private static final int MAX_ATTEMPTS = 5;
    private static final long BLOCK_DURATION_MINUTES = 15;

    private final RedisTemplate<String, Integer> redisTemplate;

    public LoginAttemptService(RedisTemplate<String, Integer> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }


    public void loginFailed(String email) {
        String key = KEY_PREFIX + email;
        Integer attempts = redisTemplate.opsForValue().get(key);
        if (attempts == null) {
            attempts = 0;
        }
        attempts++;
        redisTemplate.opsForValue().set(key, attempts, Duration.ofMinutes(BLOCK_DURATION_MINUTES));
    }


    public boolean isBlocked(String email) {
        String key = KEY_PREFIX + email;
        Integer attempts = redisTemplate.opsForValue().get(key);
        return attempts != null && attempts >= MAX_ATTEMPTS;
    }


    public void loginSucceeded(String email) {
        redisTemplate.delete(KEY_PREFIX + email);
    }
}