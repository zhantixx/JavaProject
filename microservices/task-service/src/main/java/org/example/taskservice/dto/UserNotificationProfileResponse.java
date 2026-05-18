package org.example.taskservice.dto;

public class UserNotificationProfileResponse {
    private Long id;
    private String email;
    private String name;
    private Boolean emailNotificationsEnabled;
    private Boolean pushNotificationsEnabled;
    private Integer reminderWindowMinutes;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Boolean getEmailNotificationsEnabled() { return emailNotificationsEnabled; }
    public void setEmailNotificationsEnabled(Boolean emailNotificationsEnabled) { this.emailNotificationsEnabled = emailNotificationsEnabled; }

    public Boolean getPushNotificationsEnabled() { return pushNotificationsEnabled; }
    public void setPushNotificationsEnabled(Boolean pushNotificationsEnabled) { this.pushNotificationsEnabled = pushNotificationsEnabled; }

    public Integer getReminderWindowMinutes() { return reminderWindowMinutes; }
    public void setReminderWindowMinutes(Integer reminderWindowMinutes) { this.reminderWindowMinutes = reminderWindowMinutes; }
}
