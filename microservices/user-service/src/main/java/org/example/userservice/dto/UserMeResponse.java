package org.example.userservice.dto;

public class UserMeResponse {
    private Long id;
    private String email;
    private String name;
    private String role;
    private String avatarUrl;
    private String phone;
    private String createdAt;
    private Boolean passwordChangeAvailable;
    private Boolean emailNotificationsEnabled;
    private Boolean pushNotificationsEnabled;
    private Integer reminderWindowMinutes;

    public UserMeResponse() {}

    public UserMeResponse(Long id, String email, String name, String role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
    }

    public UserMeResponse(Long id, String email, String name, String role, String avatarUrl, String createdAt, Boolean passwordChangeAvailable) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
        this.passwordChangeAvailable = passwordChangeAvailable;
    }

    public UserMeResponse(Long id,
                          String email,
                          String name,
                          String role,
                          String avatarUrl,
                          String phone,
                          String createdAt,
                          Boolean passwordChangeAvailable,
                          Boolean emailNotificationsEnabled,
                          Boolean pushNotificationsEnabled,
                          Integer reminderWindowMinutes) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.role = role;
        this.avatarUrl = avatarUrl;
        this.phone = phone;
        this.createdAt = createdAt;
        this.passwordChangeAvailable = passwordChangeAvailable;
        this.emailNotificationsEnabled = emailNotificationsEnabled;
        this.pushNotificationsEnabled = pushNotificationsEnabled;
        this.reminderWindowMinutes = reminderWindowMinutes;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getPhone() { return phone; }
    public String getCreatedAt() { return createdAt; }
    public Boolean getPasswordChangeAvailable() { return passwordChangeAvailable; }
    public Boolean getEmailNotificationsEnabled() { return emailNotificationsEnabled; }
    public Boolean getPushNotificationsEnabled() { return pushNotificationsEnabled; }
    public Integer getReminderWindowMinutes() { return reminderWindowMinutes; }

    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
    public void setRole(String role) { this.role = role; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setPhone(String phone) { this.phone = phone; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setPasswordChangeAvailable(Boolean passwordChangeAvailable) { this.passwordChangeAvailable = passwordChangeAvailable; }
    public void setEmailNotificationsEnabled(Boolean emailNotificationsEnabled) { this.emailNotificationsEnabled = emailNotificationsEnabled; }
    public void setPushNotificationsEnabled(Boolean pushNotificationsEnabled) { this.pushNotificationsEnabled = pushNotificationsEnabled; }
    public void setReminderWindowMinutes(Integer reminderWindowMinutes) { this.reminderWindowMinutes = reminderWindowMinutes; }
}
