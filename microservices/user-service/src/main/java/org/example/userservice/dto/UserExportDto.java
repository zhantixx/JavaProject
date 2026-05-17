package org.example.userservice.dto;

public class UserExportDto {
    private Long id;
    private String email;
    private String name;
    private String avatarUrl;
    private String createdAt;

    public UserExportDto() {}

    public UserExportDto(Long id, String email, String name, String avatarUrl, String createdAt) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getAvatarUrl() { return avatarUrl; }
    public String getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}
