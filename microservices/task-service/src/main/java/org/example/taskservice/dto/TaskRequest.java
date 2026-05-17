package org.example.taskservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.List;

public class TaskRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 255, message = "Title length must be <= 255")
    private String title;

    @Size(max = 5000, message = "Description length must be <= 5000")
    private String description;

    @Size(max = 32, message = "Priority length must be <= 32")
    @Pattern(regexp = "HIGH|MEDIUM|LOW", message = "Priority must be HIGH, MEDIUM or LOW")
    private String priority;

    @Size(max = 32, message = "Status length must be <= 32")
    @Pattern(regexp = "PLANNED|DONE|CANCELED", message = "Status must be PLANNED, DONE or CANCELED")
    private String status;

    private LocalDateTime deadline;

    private List<Long> tagIds;

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

    public List<Long> getTagIds() { return tagIds; }
    public void setTagIds(List<Long> tagIds) { this.tagIds = tagIds; }
}
