package org.example.userservice.dto;

public class UpdateMeRequest {
    private String name;
    private String avatarUrl;
    private String phone;
    private Boolean emailNotificationsEnabled;
    private Boolean pushNotificationsEnabled;
    private Integer reminderWindowMinutes;

    public UpdateMeRequest() {}

    public UpdateMeRequest(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Boolean getEmailNotificationsEnabled() { return emailNotificationsEnabled; }
    public void setEmailNotificationsEnabled(Boolean emailNotificationsEnabled) { this.emailNotificationsEnabled = emailNotificationsEnabled; }

    public Boolean getPushNotificationsEnabled() { return pushNotificationsEnabled; }
    public void setPushNotificationsEnabled(Boolean pushNotificationsEnabled) { this.pushNotificationsEnabled = pushNotificationsEnabled; }

    public Integer getReminderWindowMinutes() { return reminderWindowMinutes; }
    public void setReminderWindowMinutes(Integer reminderWindowMinutes) { this.reminderWindowMinutes = reminderWindowMinutes; }
}
