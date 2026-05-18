package org.example.userservice.entity;

import jakarta.persistence.*;
import org.example.userservice.utils.EmailHashUtil;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "email_hash", unique = true, length = 64)
    private String emailHash;

    @Column(nullable = false)
    private String password;

    private String name;

    @Lob
    @Column(name = "avatar_url")
    private String avatarUrl;

    private String role = "USER";

    @Column(name = "consent_given")
    private Boolean consentGiven = false;

    @Column(name = "active")
    private Boolean active = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "deletion_requested_at")
    private LocalDateTime deletionRequestedAt;

    @Column(name = "scheduled_deletion_at")
    private LocalDateTime scheduledDeletionAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        syncEmailHash();
    }

    @PreUpdate
    protected void onUpdate() {
        syncEmailHash();
    }

    private void syncEmailHash() {
        emailHash = EmailHashUtil.sha256(email);
    }

    public User() {}

    public User(String email, String password, String name, String role, Boolean consentGiven) {
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.consentGiven = consentGiven;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        this.email = EmailHashUtil.normalize(email);
        this.emailHash = EmailHashUtil.sha256(this.email);
    }

    public String getEmailHash() { return emailHash; }
    public void setEmailHash(String emailHash) { this.emailHash = emailHash; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Boolean getConsentGiven() { return consentGiven; }
    public void setConsentGiven(Boolean consentGiven) { this.consentGiven = consentGiven; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getDeletionRequestedAt() { return deletionRequestedAt; }
    public void setDeletionRequestedAt(LocalDateTime deletionRequestedAt) { this.deletionRequestedAt = deletionRequestedAt; }

    public LocalDateTime getScheduledDeletionAt() { return scheduledDeletionAt; }
    public void setScheduledDeletionAt(LocalDateTime scheduledDeletionAt) { this.scheduledDeletionAt = scheduledDeletionAt; }
}
