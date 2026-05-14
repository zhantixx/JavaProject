package org.example.taskservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public class TaskRequest {
    private String title;
    private String description;
    private String priority;
    private String status;
    private LocalDateTime deadline;
    // Идентификаторы тегов, которые следует привязать к задаче
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