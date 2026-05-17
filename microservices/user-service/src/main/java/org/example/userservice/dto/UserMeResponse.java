package org.example.userservice.dto;

public class UserMeResponse {
    private Long id;
    private String email;
    private String name;
    private String role;
    private String avatarUrl;
    private String createdAt;
    private Boolean passwordChangeAvailable;

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

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getRole() { return role; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getCreatedAt() { return createdAt; }
    public Boolean getPasswordChangeAvailable() { return passwordChangeAvailable; }

    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
    public void setRole(String role) { this.role = role; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
    public void setPasswordChangeAvailable(Boolean passwordChangeAvailable) { this.passwordChangeAvailable = passwordChangeAvailable; }
}
