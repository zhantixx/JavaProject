package org.example.userservice.dto;

public class UpdateMeRequest {
    private String name;
    private String avatarUrl;

    public UpdateMeRequest() {}

    public UpdateMeRequest(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}
