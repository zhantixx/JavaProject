package org.example.notificationservice.dto;

import org.example.notificationservice.entity.NotificationEntity;

public class NotificationItemResponse {
    private Long id;
    private Long userId;
    private String type;
    private String title;
    private String content;
    private String status;
    private String createdAt;
    private String sentAt;
    private String readAt;

    public static NotificationItemResponse from(NotificationEntity entity) {
        NotificationItemResponse response = new NotificationItemResponse();
        response.setId(entity.getId());
        response.setUserId(entity.getUserId());
        response.setType(entity.getType() == null ? null : entity.getType().name());
        response.setTitle(entity.getTitle());
        response.setContent(entity.getContent());
        response.setStatus(entity.getStatus() == null ? null : entity.getStatus().name());
        response.setCreatedAt(entity.getCreatedAt() == null ? null : entity.getCreatedAt().toString());
        response.setSentAt(entity.getSentAt() == null ? null : entity.getSentAt().toString());
        response.setReadAt(entity.getReadAt() == null ? null : entity.getReadAt().toString());
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public String getSentAt() { return sentAt; }
    public void setSentAt(String sentAt) { this.sentAt = sentAt; }
    public String getReadAt() { return readAt; }
    public void setReadAt(String readAt) { this.readAt = readAt; }
}
