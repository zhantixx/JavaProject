package org.example.userservice.service;

import org.example.userservice.entity.User;
import org.example.userservice.repository.AuditLogRepository;
import org.example.userservice.repository.EventRepository;
import org.example.userservice.repository.NoteRepository;
import org.example.userservice.repository.OAuthAccountRepository;
import org.example.userservice.repository.RefreshTokenRepository;
import org.example.userservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class AccountDeletionScheduler {

    private final UserRepository userRepository;
    private final NoteRepository noteRepository;
    private final EventRepository eventRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuditLogRepository auditLogRepository;
    private final RestTemplate restTemplate;

    @Value("${task.service.url:http://task-service:8082}")
    private String taskServiceUrl;

    @Value("${internal.api.secret}")
    private String internalApiSecret;

    public AccountDeletionScheduler(UserRepository userRepository,
                                    NoteRepository noteRepository,
                                    EventRepository eventRepository,
                                    OAuthAccountRepository oAuthAccountRepository,
                                    RefreshTokenRepository refreshTokenRepository,
                                    AuditLogRepository auditLogRepository,
                                    RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.noteRepository = noteRepository;
        this.eventRepository = eventRepository;
        this.oAuthAccountRepository = oAuthAccountRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.auditLogRepository = auditLogRepository;
        this.restTemplate = restTemplate;
    }

    @Scheduled(cron = "${app.gdpr.deletion-cron:0 0 * * * *}")
    @Transactional
    public void purgeExpiredAccounts() {
        List<User> dueUsers = userRepository.findAllByScheduledDeletionAtBefore(LocalDateTime.now());
        for (User user : dueUsers) {
            deleteRemoteTasks(user.getId());
            noteRepository.deleteByUserId(user.getId());
            eventRepository.deleteByUserId(user.getId());
            refreshTokenRepository.deleteByUserId(user.getId());
            oAuthAccountRepository.deleteByEmail(user.getEmail());
            auditLogRepository.deleteByUserId(user.getId());
            userRepository.delete(user);
        }
    }

    private void deleteRemoteTasks(Long userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Secret", internalApiSecret);
        restTemplate.exchange(
                taskServiceUrl + "/api/tasks/user/" + userId,
                HttpMethod.DELETE,
                new HttpEntity<>(headers),
                Void.class
        );
    }
}
