package org.example.userservice.dto;

public class UpdateMeRequest {
    private String name;

    public UpdateMeRequest() {}

    public UpdateMeRequest(String name) {
        this.name = name;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
