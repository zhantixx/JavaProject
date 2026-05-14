package org.example.taskservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "tags",
        indexes = {
                @Index(name = "idx_tags_user", columnList = "user_id")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_tags_user_name", columnNames = {"user_id", "name"})
        }
)
public class Tag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Владелец тега (идентификатор пользователя)
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Название тега (уникально в рамках одного пользователя)
    @Column(nullable = false, length = 64)
    private String name;

    // Опциональный цвет тега в hex (например, #FF9900)
    @Column(length = 16)
    private String color;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected Tag() {
        // JPA
    }

    public Tag(Long userId, String name, String color) {
        this.userId = userId;
        this.name = name;
        this.color = color;
    }

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name != null ? name.trim() : null; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color != null ? color.trim() : null; }

    public LocalDateTime getCreatedAt() { return createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
