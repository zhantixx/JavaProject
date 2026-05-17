package org.example.taskservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(
        name = "tasks",
        indexes = {
                @Index(name = "idx_tasks_user", columnList = "user_id"),
                @Index(name = "idx_tasks_user_deadline", columnList = "user_id,deadline")
        }
)
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String description;
    private String priority;
    private String status;

    @Column(name = "deadline")
    private LocalDateTime deadline;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "reminder_sent")
    private Boolean reminderSent = false;

    @Column(name = "overdue_sent")
    private Boolean overdueSent = false;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "task_tags",
            joinColumns = @JoinColumn(name = "task_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"),
            uniqueConstraints = {
                    @UniqueConstraint(name = "uk_task_tag", columnNames = {"task_id", "tag_id"})
            }
    )
    private Set<Tag> tags = new HashSet<>();

    public Task() {}

    public Task(String title, String description, String priority, String status, LocalDateTime deadline, Long userId) {
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.status = status;
        this.deadline = deadline;
        this.userId = userId;
        this.reminderSent = false;
        this.overdueSent = false;
    }

    @PrePersist
    @PreUpdate
    private void normalizeFlags() {
        if (reminderSent == null) {
            reminderSent = false;
        }
        if (overdueSent == null) {
            overdueSent = false;
        }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getPriority() { return priority; }
    public void setPriority(String priority) { this.priority = priority; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getDeadline() { return deadline; }
    public void setDeadline(LocalDateTime deadline) { this.deadline = deadline; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public boolean isReminderSent() { return Boolean.TRUE.equals(reminderSent); }
    public void setReminderSent(Boolean reminderSent) { this.reminderSent = reminderSent != null ? reminderSent : false; }
    public boolean isOverdueSent() { return Boolean.TRUE.equals(overdueSent); }
    public void setOverdueSent(Boolean overdueSent) { this.overdueSent = overdueSent != null ? overdueSent : false; }

    public Set<Tag> getTags() { return tags; }
    public void setTags(Set<Tag> tags) { this.tags = tags != null ? tags : new HashSet<>(); }
}
